package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.dto.response.RecommendResp;
import com.example.coffee.dto.response.SpecResp;
import com.example.coffee.entity.OrderItem;
import com.example.coffee.entity.Product;
import com.example.coffee.entity.ProductSpec;
import com.example.coffee.entity.UserBehavior;
import com.example.coffee.mapper.OrderItemMapper;
import com.example.coffee.mapper.ProductMapper;
import com.example.coffee.mapper.ProductSpecMapper;
import com.example.coffee.mapper.UserBehaviorMapper;
import com.example.coffee.service.RecommendService;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private static final int K = 20;
    private static final Map<String, Integer> WEIGHTS = Map.of(
            "view", 1, "cart", 2, "order", 3, "favorite", 5);

    private final UserBehaviorMapper userBehaviorMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;
    private final OrderItemMapper orderItemMapper;
    private final RedisUtil redisUtil;

    @Override
    public List<RecommendResp> recommend(Long userId, Long storeId, int size) {
        size = Math.min(size, 20);
        String cacheKey = "recommend:user:" + userId + ":store:" + storeId;

        @SuppressWarnings("unchecked")
        List<RecommendResp> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached.stream().limit(size).collect(Collectors.toList());
        }

        // 尝试协同过滤推荐
        Map<Long, Double> targetVector = getUserVector(userId);
        if (targetVector.isEmpty()) {
            return getHotProducts(storeId, size);
        }

        Set<Long> interactedProductIds = targetVector.keySet();
        List<UserBehavior> relatedBehaviors = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .in(UserBehavior::getProductId, interactedProductIds)
                        .ne(UserBehavior::getUserId, userId));
        Set<Long> neighborIds = relatedBehaviors.stream()
                .map(UserBehavior::getUserId).collect(Collectors.toSet());
        if (neighborIds.isEmpty()) {
            return getHotProducts(storeId, size);
        }

        PriorityQueue<Neighbor> topK = new PriorityQueue<>(
                Comparator.comparingDouble(Neighbor::getSimilarity));
        for (Long neighborId : neighborIds) {
            Map<Long, Double> neighborVector = getUserVector(neighborId);
            double sim = cosineSimilarity(targetVector, neighborVector);
            topK.offer(new Neighbor(neighborId, sim));
            if (topK.size() > K) topK.poll();
        }

        Map<Long, Double> predictions = new HashMap<>();
        for (Neighbor neighbor : topK) {
            Map<Long, Double> neighborVector = getUserVector(neighbor.getUserId());
            for (Map.Entry<Long, Double> entry : neighborVector.entrySet()) {
                Long productId = entry.getKey();
                if (!interactedProductIds.contains(productId)) {
                    predictions.merge(productId,
                            entry.getValue() * neighbor.getSimilarity(), Double::sum);
                }
            }
        }

        List<RecommendResp> result = predictions.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(e -> buildRecommendResp(e.getKey(), storeId, e.getValue(), "与你相似的用户也喜欢"))
                .filter(Objects::nonNull)
                .limit(size)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return getHotProducts(storeId, size);
        }

        redisUtil.set(cacheKey, result, 6, TimeUnit.HOURS);
        return result;
    }

    /**
     * 基于订单热度的推荐（无用户行为时使用）
     */
    @Override
    public List<RecommendResp> recommendByStore(Long storeId, int size) {
        size = Math.min(size, 20);
        String cacheKey = "recommend:store:" + storeId + ":size:" + size;

        @SuppressWarnings("unchecked")
        List<RecommendResp> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<RecommendResp> result = getHotProductsFromOrders(storeId, size);

        redisUtil.set(cacheKey, result, 30, TimeUnit.MINUTES);
        return result;
    }

    private List<RecommendResp> getHotProductsFromOrders(Long storeId, int size) {
        // 查询该门店下所有产品
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStoreId, storeId)
                        .eq(Product::getStatus, 1));

        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询该门店商品在 order_item 中的出现次数
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getProductId, productIds));

        // 按 productId 统计出现次数
        Map<Long, Long> productCount = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getProductId, Collectors.counting()));

        // 排序：按出现次数降序，取前 size 个
        List<Product> sorted = products.stream()
                .sorted((a, b) -> {
                    long countB = productCount.getOrDefault(b.getId(), 0L);
                    long countA = productCount.getOrDefault(a.getId(), 0L);
                    return Long.compare(countB, countA);
                })
                .limit(size)
                .collect(Collectors.toList());

        return sorted.stream().map(p -> {
            long count = productCount.getOrDefault(p.getId(), 0L);
            if (count > 0) {
                return buildRecommendResp(p.getId(), storeId, (double) count, "门店热销");
            } else {
                return buildRecommendResp(p.getId(), storeId, (double) p.getSales(), "新品推荐");
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<RecommendResp> getHotProducts(Long storeId, int size) {
        List<Product> hotProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStoreId, storeId)
                        .eq(Product::getStatus, 1)
                        .orderByDesc(Product::getSales)
                        .last("LIMIT " + size));
        return hotProducts.stream().map(p ->
                buildRecommendResp(p.getId(), storeId, (double) p.getSales(), "门店热销"))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private RecommendResp buildRecommendResp(Long productId, Long storeId, Double score, String reason) {
        Product product = productMapper.selectById(productId);
        if (product == null || !product.getStoreId().equals(storeId) || product.getStatus() == 0) {
            return null;
        }
        RecommendResp resp = new RecommendResp();
        resp.setProductId(product.getId());
        resp.setStoreId(product.getStoreId());
        resp.setProductName(product.getName());
        resp.setCover(product.getImage());
        resp.setScore(Math.round(score * 100.0) / 100.0);
        resp.setReason(reason);

        // 查询规格和最低价（基础售价 + 最小价格调整）
        List<ProductSpec> specs = productSpecMapper.selectList(
                new LambdaQueryWrapper<ProductSpec>()
                        .eq(ProductSpec::getProductId, productId)
                        .eq(ProductSpec::getStatus, 1));
        if (product.getPrice() != null) {
            BigDecimal minAdjust = specs.stream()
                    .map(ProductSpec::getPriceAdjust)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            resp.setMinPrice(product.getPrice().add(minAdjust));
        }
        if (!specs.isEmpty()) {
            resp.setSpecs(specs.stream().map(s -> {
                SpecResp sr = new SpecResp();
                sr.setId(s.getId());
                sr.setProductId(s.getProductId());
                sr.setSpecName(s.getSpecName());
                sr.setSpecValue(s.getSpecValue());
                sr.setPriceAdjust(s.getPriceAdjust());
                sr.setStatus(s.getStatus());
                return sr;
            }).collect(Collectors.toList()));
        }

        return resp;
    }

    private Map<Long, Double> getUserVector(Long userId) {
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>().eq(UserBehavior::getUserId, userId));
        Map<Long, Double> vector = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            int weight = WEIGHTS.getOrDefault(behavior.getType(), 1);
            double score = (double) (weight * behavior.getCount());
            if (behavior.getScore() != null && behavior.getScore() > 0) {
                score += behavior.getScore() * 0.5;
            }
            vector.merge(behavior.getProductId(), score, Double::sum);
        }
        return vector;
    }

    private double cosineSimilarity(Map<Long, Double> v1, Map<Long, Double> v2) {
        double dotProduct = 0, norm1 = 0, norm2 = 0;
        for (Map.Entry<Long, Double> entry : v1.entrySet()) {
            double val1 = entry.getValue();
            double val2 = v2.getOrDefault(entry.getKey(), 0.0);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
        }
        for (double val2 : v2.values()) {
            norm2 += val2 * val2;
        }
        if (norm1 == 0 || norm2 == 0) return 0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private static class Neighbor {
        private final Long userId;
        private final double similarity;

        Neighbor(Long userId, double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }

        public Long getUserId() { return userId; }
        public double getSimilarity() { return similarity; }
    }
}
