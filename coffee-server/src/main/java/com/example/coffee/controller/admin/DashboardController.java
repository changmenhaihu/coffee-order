package com.example.coffee.controller.admin;

import com.example.coffee.common.Result;
import com.example.coffee.dto.response.DashboardResp;
import com.example.coffee.service.DashboardService;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 管理后台 — 数据概览
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final RedisUtil redisUtil;

    private static final String DASHBOARD_CACHE_KEY = "dashboard:data";
    private static final long CACHE_TTL = 1;

    @GetMapping("/dashboard")
    public Result<DashboardResp> dashboard() {
        DashboardResp cached = redisUtil.get(DASHBOARD_CACHE_KEY);
        if (cached != null) {
            return Result.success(cached);
        }
        DashboardResp resp = dashboardService.getDashboard();
        redisUtil.set(DASHBOARD_CACHE_KEY, resp, CACHE_TTL, TimeUnit.MINUTES);
        return Result.success(resp);
    }
}
