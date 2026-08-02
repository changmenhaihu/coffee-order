package com.example.coffee.service;

import com.example.coffee.dto.request.AddCartReq;
import com.example.coffee.dto.request.UpdateCartReq;
import com.example.coffee.dto.response.CartResp;

public interface CartService {

    CartResp getCart(Long userId);

    void addToCart(Long userId, AddCartReq req);

    void updateCart(Long userId, Long cartId, UpdateCartReq req);

    void deleteCartItem(Long userId, Long cartId);

    void clearCart(Long userId);
}
