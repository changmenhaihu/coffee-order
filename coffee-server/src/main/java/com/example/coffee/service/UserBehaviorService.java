package com.example.coffee.service;

public interface UserBehaviorService {

    void recordBehavior(Long userId, Long productId, Long storeId, String type, Integer score);
}
