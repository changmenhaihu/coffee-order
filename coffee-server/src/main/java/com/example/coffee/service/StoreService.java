package com.example.coffee.service;

import com.example.coffee.common.PageResult;
import com.example.coffee.dto.response.StoreDetailResp;
import com.example.coffee.dto.response.StoreNearbyResp;

import java.math.BigDecimal;

public interface StoreService {

    PageResult<StoreNearbyResp> nearbyStores(BigDecimal lng, BigDecimal lat, Integer radius,
                                              String keyword, int page, int size);

    StoreDetailResp getStoreDetail(Long storeId);
}
