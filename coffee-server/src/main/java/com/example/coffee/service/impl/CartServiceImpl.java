package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.request.AddCartReq;
import com.example.coffee.dto.request.UpdateCartReq;
import com.example.coffee.dto.response.CartItemResp;
import com.example.coffee.dto.response.CartResp;
import com.example.coffee.entity.*;
import com.example.coffee.mapper.*;
import com.example.coffee.service.CartService;
import com.example.coffee.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;
    private final StoreMapper storeMapper;
    private final UserBehaviorService userBehaviorService;

    @Override
    public CartResp getCart(Long userId) {
        List<Cart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));

        if (carts.isEmpty()) {
            CartResp empty = new CartResp();
            empty.setItems(new ArrayList<>());
            empty.setTotalCount(0);
            empty.setTotalAmount(BigDecimal.ZERO);
            return empty;
        }

        // 校验门店一致性
        long storeCount = carts.stream().map(Cart::getStoreId).distinct().count();
        if (storeCount > 1) {
            cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
            CartResp empty = new CartResp();
            empty.setItems(new ArrayList<>());
            empty.setTotalCount(0);
            empty.setTotalAmount(BigDecimal.ZERO);
            return empty;
        }

        Long storeId = carts.get(0).getStoreId();
        Store store = storeMapper.selectById(storeId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;
        List<CartItemResp> items = new ArrayList<>();

        for (Cart cart : carts) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) continue;

            CartItemResp item = new CartItemResp();
            item.setCartId(cart.getId());
            item.setProductId(product.getId());
            item.setStoreId(cart.getStoreId());
            item.setProductName(product.getName());
            item.setImage(product.getImage());
            item.setSpecInfo(cart.getSpecInfo());
            item.setPrice(cart.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            items.add(item);

            totalAmount = totalAmount.add(item.getSubtotal());
            totalCount += cart.getQuantity();
        }

        CartResp resp = new CartResp();
        resp.setStoreId(storeId);
        resp.setStoreName(store != null ? store.getName() : null);
        resp.setItems(items);
        resp.setTotalCount(totalCount);
        resp.setTotalAmount(totalAmount);
        return resp;
    }

    @Override
    @Transactional
    public void addToCart(Long userId, AddCartReq req) {
        Product product = productMapper.selectById(req.getProductId());
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }

        // 加载所选规格，拼接 spec_info 并计算单价
        List<ProductSpec> specs = req.getSpecIds().stream()
                .map(productSpecMapper::selectById)
                .filter(s -> s != null && s.getStatus() == 1)
                .collect(Collectors.toList());
        if (specs.size() != req.getSpecIds().size()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "规格不存在或已失效");
        }
        String specInfo = specs.stream()
                .map(ProductSpec::getSpecValue)
                .collect(Collectors.joining("/"));
        BigDecimal unitPrice = product.getPrice().add(
                specs.stream().map(ProductSpec::getPriceAdjust)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 校验门店一致性
        List<Cart> existingCarts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        if (!existingCarts.isEmpty()) {
            Long existingStoreId = existingCarts.get(0).getStoreId();
            if (!existingStoreId.equals(req.getStoreId())) {
                throw new BusinessException(ResultCode.CONFLICT,
                        "请先清空购物车再添加其他门店商品");
            }
        }

        // 存在则更新数量，不存在则新增
        Cart existing = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getStoreId, req.getStoreId())
                .eq(Cart::getProductId, req.getProductId())
                .eq(Cart::getSpecInfo, specInfo));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            existing.setPrice(unitPrice);
            cartMapper.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setStoreId(req.getStoreId());
            cart.setProductId(req.getProductId());
            cart.setSpecInfo(specInfo);
            cart.setPrice(unitPrice);
            cart.setQuantity(req.getQuantity());
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }

        userBehaviorService.recordBehavior(userId, req.getProductId(), req.getStoreId(), "cart", null);
    }

    @Override
    @Transactional
    public void updateCart(Long userId, Long cartId, UpdateCartReq req) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "购物车记录不存在");
        }
        if (req.getQuantity() <= 0) {
            cartMapper.deleteById(cartId);
            return;
        }
        cart.setQuantity(req.getQuantity());
        cartMapper.updateById(cart);
    }

    @Override
    public void deleteCartItem(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "购物车记录不存在");
        }
        cartMapper.deleteById(cartId);
    }

    @Override
    public void clearCart(Long userId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }
}
