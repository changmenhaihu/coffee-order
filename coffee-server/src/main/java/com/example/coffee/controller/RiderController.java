package com.example.coffee.controller;

import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.dto.request.RiderLocationReq;
import com.example.coffee.dto.response.RiderOrderResp;
import com.example.coffee.service.RiderService;
import com.example.coffee.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RIDER')")
public class RiderController {

    private final RiderService riderService;

    @GetMapping("/orders/pending")
    public Result<PageResult<RiderOrderResp>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(riderService.getPendingOrders(page, size));
    }

    @PostMapping("/orders/{orderId}/accept")
    public Result<Void> accept(@PathVariable Long orderId) {
        riderService.acceptOrder(UserContext.getUserId(), orderId);
        return Result.success("接单成功", null);
    }

    @PostMapping("/location")
    public Result<Void> reportLocation(@Valid @RequestBody RiderLocationReq req) {
        riderService.reportLocation(UserContext.getUserId(), req);
        return Result.success();
    }

    @PostMapping("/orders/{orderId}/pickup")
    public Result<Void> pickup(@PathVariable Long orderId) {
        riderService.pickupOrder(UserContext.getUserId(), orderId);
        return Result.success();
    }

    @PostMapping("/orders/{orderId}/deliver")
    public Result<Void> deliver(@PathVariable Long orderId) {
        riderService.deliverOrder(UserContext.getUserId(), orderId);
        return Result.success();
    }

    @GetMapping("/orders/tasks")
    public Result<List<RiderOrderResp>> tasks() {
        return Result.success(riderService.getTasks(UserContext.getUserId()));
    }
}
