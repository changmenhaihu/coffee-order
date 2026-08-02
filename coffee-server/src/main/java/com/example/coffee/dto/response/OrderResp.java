package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResp {

    private Long orderId;
    private String orderNo;
    private String storeName;
    private Integer pickupType;
    private Integer status;
    private String statusText;
    private BigDecimal totalPrice;
    private BigDecimal deliveryFee;
    private BigDecimal payAmount;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
