package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.response.*;
import com.example.coffee.entity.*;
import com.example.coffee.mapper.*;
import com.example.coffee.service.ProductService;
import com.example.coffee.service.UserBehaviorService;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final StoreMapper storeMapper;
    private final EvaluationMapper evaluationMapper;
    private final RedisUtil redisUtil;
    private final UserBehaviorService userBehaviorService;

    @Override
    public List<CategoryResp> getCategories(Long storeId) {
        String cacheKey = "product:categories:" + storeId;
        @SuppressWarnings("unchecked")
        List<CategoryResp> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<ProductCategory> categories = productCategoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getStoreId, storeId)
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder));

        List<CategoryResp> result = categories.stream().map(c -> {
            CategoryResp resp = new CategoryResp();
            resp.setId(c.getId());
            resp.setStoreId(c.getStoreId());
            resp.setName(c.getName());
            resp.setSortOrder(c.getSortOrder());
            return resp;
        }).collect(Collectors.toList());

        redisUtil.set(cacheKey, result, 30, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public PageResult<ProductListResp> getProductList(Long storeId, Long categoryId,
                                                       String keyword, int page, int size) {
        size = Math.min(size, 50);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStoreId, storeId)
                .eq(Product::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Product::getName, keyword);
        }
        wrapper.orderByDesc(Product::getSales).orderByAsc(Product::getSortOrder);

        Page<Product> mpPage = new Page<>(page, size);
        Page<Product> result = productMapper.selectPage(mpPage, wrapper);

        List<ProductListResp> records = result.getRecords().stream()
                .map(this::toProductListResp)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), records);
    }

    @Override
    public ProductDetailResp getProductDetail(Long productId, Long userId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }

        ProductDetailResp resp = new ProductDetailResp();
        resp.setId(product.getId());
        resp.setStoreId(product.getStoreId());
        resp.setCategoryId(product.getCategoryId());
        resp.setName(product.getName());
        resp.setImage(product.getImage());
        resp.setPrice(product.getPrice());
        resp.setDescription(product.getDescription());
        resp.setIsRecommend(product.getIsRecommend());
        resp.setSales(product.getSales());
        resp.setRating(product.getRating());
        resp.setRatingCount(product.getRatingCount());
        resp.setSortOrder(product.getSortOrder());

        // 门店名称
        Store store = storeMapper.selectById(product.getStoreId());
        resp.setStoreName(store != null ? store.getName() : null);

        // 分类名称
        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        resp.setCategoryName(category != null ? category.getName() : null);

        // 规格（按维度分组返回）
        List<ProductSpec> specs = productSpecMapper.selectList(
                new LambdaQueryWrapper<ProductSpec>()
                        .eq(ProductSpec::getProductId, productId)
                        .eq(ProductSpec::getStatus, 1));
        resp.setSpecs(specs.stream().map(this::toSpecResp).collect(Collectors.toList()));

        // 最近评价
        List<Evaluation> evaluations = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getProductId, productId)
                        .eq(Evaluation::getStatus, 1)
                        .isNotNull(Evaluation::getContent)
                        .orderByDesc(Evaluation::getCreateTime)
                        .last("LIMIT 3"));
        resp.setRecentEvaluations(evaluations.stream().map(e -> {
            EvaluationResp er = new EvaluationResp();
            er.setId(e.getId());
            er.setUserId(e.getUserId());
            er.setNickname(maskNickname(e.getUserId()));
            er.setScore(e.getScore());
            er.setContent(e.getContent());
            er.setCreateTime(e.getCreateTime());
            return er;
        }).collect(Collectors.toList()));

        // 记录浏览行为
        if (userId != null) {
            userBehaviorService.recordBehavior(userId, productId, product.getStoreId(), "view", null);
        }

        return resp;
    }

    private ProductListResp toProductListResp(Product product) {
        ProductListResp resp = new ProductListResp();
        resp.setId(product.getId());
        resp.setStoreId(product.getStoreId());
        resp.setCategoryId(product.getCategoryId());
        resp.setName(product.getName());
        resp.setImage(product.getImage());
        resp.setPrice(product.getPrice());
        resp.setDescription(product.getDescription());
        resp.setIsRecommend(product.getIsRecommend());
        resp.setSales(product.getSales());
        resp.setRating(product.getRating());
        resp.setRatingCount(product.getRatingCount());
        resp.setSortOrder(product.getSortOrder());

        // 规格
        List<ProductSpec> specs = productSpecMapper.selectList(
                new LambdaQueryWrapper<ProductSpec>()
                        .eq(ProductSpec::getProductId, product.getId())
                        .eq(ProductSpec::getStatus, 1));
        resp.setSpecs(specs.stream().map(this::toSpecResp).collect(Collectors.toList()));

        // 分类名称
        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        resp.setCategoryName(category != null ? category.getName() : null);

        return resp;
    }

    private SpecResp toSpecResp(ProductSpec s) {
        SpecResp sr = new SpecResp();
        sr.setId(s.getId());
        sr.setProductId(s.getProductId());
        sr.setSpecName(s.getSpecName());
        sr.setSpecValue(s.getSpecValue());
        sr.setPriceAdjust(s.getPriceAdjust());
        sr.setStatus(s.getStatus());
        return sr;
    }

    private String maskNickname(Long userId) {
        return "用户***" + (userId % 100);
    }
}
