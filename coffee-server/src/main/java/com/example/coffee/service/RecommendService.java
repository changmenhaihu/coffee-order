package com.example.coffee.service;

import com.example.coffee.dto.response.RecommendResp;

import java.util.List;

public interface RecommendService {

    List<RecommendResp> recommend(Long userId, Long storeId, int size);

    List<RecommendResp> recommendByStore(Long storeId, int size);
}
