package com.example.coffee.controller;

import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.dto.response.StoreDetailResp;
import com.example.coffee.dto.response.StoreNearbyResp;
import com.example.coffee.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/nearby")
    public Result<PageResult<StoreNearbyResp>> nearby(
            @RequestParam BigDecimal lng,
            @RequestParam BigDecimal lat,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(storeService.nearbyStores(lng, lat, radius, keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<StoreDetailResp> detail(@PathVariable Long id) {
        return Result.success(storeService.getStoreDetail(id));
    }
}
