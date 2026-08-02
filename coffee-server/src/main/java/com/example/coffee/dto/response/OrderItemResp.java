package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResp {

    private Long id;
    private Long productId;
    private String productName;
    private String specInfo;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal total;
}
