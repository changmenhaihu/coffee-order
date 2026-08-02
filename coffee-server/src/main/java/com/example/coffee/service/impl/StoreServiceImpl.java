package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.response.*;
import com.example.coffee.entity.*;
import com.example.coffee.mapper.*;
import com.example.coffee.service.StoreService;
import com.example.coffee.util.GeoUtil;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;
    private final RedisUtil redisUtil;

    @Override
    public PageResult<StoreNearbyResp> nearbyStores(BigDecimal lng, BigDecimal lat,
                                                     Integer radius, String keyword, int page, int size) {
        final int finalRadius = (radius == null || radius <= 0) ? 5000 : Math.min(radius, 20000);
        final int finalSize = Math.min(size, 50);

        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<Store>()
                .eq(Store::getStatus, 1);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Store::getName, keyword);
        }
        List<Store> allStores = storeMapper.selectList(wrapper);

        List<StoreNearbyResp> respList = allStores.stream()
                .filter(s -> s.getLongitude() != null && s.getLatitude() != null)
                .map(s -> toNearbyResp(s, lng, lat))
                .filter(r -> r.getDistance() <= finalRadius)
                .sorted((a, b) -> Double.compare(a.getDistance(), b.getDistance()))
                .collect(Collectors.toList());

        int total = respList.size();
        int fromIndex = (page - 1) * finalSize;
        int toIndex = Math.min(fromIndex + finalSize, total);
        List<StoreNearbyResp> pageList = fromIndex < total
                ? respList.subList(fromIndex, toIndex) : Collections.emptyList();

        long pages = (total + finalSize - 1) / finalSize;
        return PageResult.of(total, pages, page, finalSize, pageList);
    }

    @Override
    public StoreDetailResp getStoreDetail(Long storeId) {
        String cacheKey = "store:detail:" + storeId;
        StoreDetailResp cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Store store = storeMapper.selectById(storeId);
        if (store == null || store.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "门店不存在");
        }

        StoreDetailResp resp = new StoreDetailResp();
        resp.setId(store.getId());
        resp.setName(store.getName());
        resp.setAddress(store.getAddress());
        resp.setPhone(store.getPhone());
        resp.setLongitude(store.getLongitude());
        resp.setLatitude(store.getLatitude());
        resp.setImage(store.getImage());
        resp.setBusinessHours(store.getBusinessHours());
        resp.setOpenTime(store.getOpenTime());
        resp.setCloseTime(store.getCloseTime());
        resp.setMinDeliveryAmount(store.getMinDeliveryAmount());
        resp.setDeliveryFee(store.getDeliveryFee());
        resp.setStatus(store.getStatus());

        List<ProductCategory> categories = productCategoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getStoreId, storeId)
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder));

        List<CategoryWithProductsResp> categoryList = new ArrayList<>();
        for (ProductCategory cat : categories) {
            CategoryWithProductsResp catResp = new CategoryWithProductsResp();
            catResp.setId(cat.getId());
            catResp.setName(cat.getName());

            List<Product> products = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .eq(Product::getCategoryId, cat.getId())
                            .eq(Product::getStatus, 1)
                            .orderByAsc(Product::getSortOrder));

            List<ProductListResp> productResps = products.stream()
                    .map(this::toProductListResp)
                    .collect(Collectors.toList());
            catResp.setProducts(productResps);
            categoryList.add(catResp);
        }
        resp.setCategories(categoryList);

        redisUtil.set(cacheKey, resp, 10, TimeUnit.MINUTES);
        return resp;
    }

    private StoreNearbyResp toNearbyResp(Store store, BigDecimal lng, BigDecimal lat) {
        StoreNearbyResp resp = new StoreNearbyResp();
        resp.setId(store.getId());
        resp.setName(store.getName());
        resp.setAddress(store.getAddress());
        resp.setPhone(store.getPhone());
        resp.setLongitude(store.getLongitude());
        resp.setLatitude(store.getLatitude());
        resp.setImage(store.getImage());
        resp.setBusinessHours(store.getBusinessHours());
        resp.setOpenTime(store.getOpenTime());
        resp.setCloseTime(store.getCloseTime());
        resp.setMinDeliveryAmount(store.getMinDeliveryAmount());
        resp.setDeliveryFee(store.getDeliveryFee());
        resp.setStatus(store.getStatus());

        double distance = GeoUtil.haversineDistance(lat.doubleValue(), lng.doubleValue(),
                store.getLatitude().doubleValue(), store.getLongitude().doubleValue());
        resp.setDistance(distance);
        resp.setDistanceText(GeoUtil.formatDistance(distance));
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
}
