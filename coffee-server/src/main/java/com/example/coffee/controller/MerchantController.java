package com.example.coffee.controller;

import com.example.coffee.common.Result;
import com.example.coffee.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STORE_MANAGER','ADMIN')")
public class MerchantController {

    private final OrderService orderService;

    @PutMapping("/orders/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        orderService.merchantAcceptOrder(id);
        return Result.success("接单成功，开始制作", null);
    }

    @PutMapping("/orders/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        orderService.merchantCompleteOrder(id);
        return Result.success("出餐成功，等待取餐", null);
    }
}
