package com.example.coffee.controller;

import com.example.coffee.common.Result;
import com.example.coffee.dto.request.AddCartReq;
import com.example.coffee.dto.request.UpdateCartReq;
import com.example.coffee.dto.response.CartResp;
import com.example.coffee.service.CartService;
import com.example.coffee.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<CartResp> getCart() {
        return Result.success(cartService.getCart(UserContext.getUserId()));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody AddCartReq req) {
        cartService.addToCart(UserContext.getUserId(), req);
        return Result.success("添加成功", null);
    }

    @PutMapping("/{cartId}")
    public Result<Void> update(@PathVariable Long cartId, @Valid @RequestBody UpdateCartReq req) {
        cartService.updateCart(UserContext.getUserId(), cartId, req);
        return Result.success();
    }

    @DeleteMapping("/{cartId}")
    public Result<Void> delete(@PathVariable Long cartId) {
        cartService.deleteCartItem(UserContext.getUserId(), cartId);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clearCart(UserContext.getUserId());
        return Result.success();
    }
}
