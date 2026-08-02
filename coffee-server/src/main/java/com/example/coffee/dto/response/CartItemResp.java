package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResp {

    private Long cartId;
    private Long productId;
    private Long storeId;
    private String productName;
    private String image;
    private String specInfo;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
