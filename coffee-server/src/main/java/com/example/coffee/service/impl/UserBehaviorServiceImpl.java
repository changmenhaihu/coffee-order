package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.entity.UserBehavior;
import com.example.coffee.mapper.UserBehaviorMapper;
import com.example.coffee.service.UserBehaviorService;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBehaviorServiceImpl implements UserBehaviorService {

    private final UserBehaviorMapper userBehaviorMapper;
    private final RedisUtil redisUtil;

    @Override
    @Async
    public void recordBehavior(Long userId, Long productId, Long storeId, String type, Integer score) {
        UserBehavior existing = userBehaviorMapper.selectOne(
                new LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getProductId, productId)
                        .eq(UserBehavior::getType, type));

        if (existing != null) {
            existing.setCount(existing.getCount() + 1);
            if (score != null && score > 0) {
                existing.setScore(score);
            }
            userBehaviorMapper.updateById(existing);
        } else {
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setProductId(productId);
            behavior.setStoreId(storeId);
            behavior.setType(type);
            behavior.setCount(1);
            behavior.setScore(score);
            userBehaviorMapper.insert(behavior);
        }

        // 清除推荐缓存（用户行为变化时）
        redisUtil.delete("recommend:user:" + userId + ":store:" + storeId);
    }
}
