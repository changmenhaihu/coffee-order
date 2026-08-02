package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResp {

    private Long storeId;
    private String storeName;
    private Integer totalCount;
    private BigDecimal totalAmount;
    private List<CartItemResp> items;
}
