package com.example.coffee.controller;

import com.example.coffee.common.Result;
import com.example.coffee.dto.response.RecommendResp;
import com.example.coffee.service.RecommendService;
import com.example.coffee.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping
    public Result<List<RecommendResp>> recommend(
            @RequestParam Long storeId,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        // 如果用户未登录，使用基于门店热度的推荐
        if (userId == null) {
            return Result.success(recommendService.recommendByStore(storeId, size));
        }
        return Result.success(recommendService.recommend(userId, storeId, size));
    }

    @GetMapping("/hot")
    public Result<List<RecommendResp>> hotProducts(
            @RequestParam Long storeId,
            @RequestParam(defaultValue = "6") int size) {
        return Result.success(recommendService.recommendByStore(storeId, size));
    }
}
