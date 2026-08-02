package com.example.coffee.controller;

import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.dto.request.CancelOrderReq;
import com.example.coffee.dto.request.CreateOrderReq;
import com.example.coffee.dto.request.PayOrderReq;
import com.example.coffee.dto.response.OrderDetailResp;
import com.example.coffee.dto.response.OrderListResp;
import com.example.coffee.dto.response.OrderResp;
import com.example.coffee.service.OrderService;
import com.example.coffee.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderResp> create(@Valid @RequestBody CreateOrderReq req) {
        OrderResp resp = orderService.createOrder(UserContext.getUserId(), req);
        return Result.success("订单创建成功", resp);
    }

    @PostMapping("/{orderId}/pay")
    public Result<Void> pay(@PathVariable Long orderId, @RequestBody PayOrderReq req) {
        orderService.payOrder(UserContext.getUserId(), orderId, req);
        return Result.success("支付成功", null);
    }

    @GetMapping("/list")
    public Result<PageResult<OrderListResp>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer pickupType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String role = getCurrentRole();
        return Result.success(orderService.getOrderList(UserContext.getUserId(), role, status, pickupType, page, size));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDetailResp> detail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(UserContext.getUserId(), orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable Long orderId, @RequestBody CancelOrderReq req) {
        orderService.cancelOrder(UserContext.getUserId(), orderId, req);
        return Result.success();
    }

    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirm(@PathVariable Long orderId) {
        orderService.confirmOrder(UserContext.getUserId(), orderId);
        return Result.success();
    }

    @PostMapping("/{orderId}/reorder")
    public Result<Void> reorder(@PathVariable Long orderId) {
        orderService.reorder(UserContext.getUserId(), orderId);
        return Result.success("已加入购物车", null);
    }

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .map(a -> a.substring(5).toUpperCase())
                    .findFirst().orElse("USER");
        }
        return "USER";
    }
}
