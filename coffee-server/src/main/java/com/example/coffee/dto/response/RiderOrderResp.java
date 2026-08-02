package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RiderOrderResp {

    private Long orderId;
    private String orderNo;
    private String storeName;
    private String storeAddress;
    private String firstItemName;
    private Integer pickupType;
    private String addressSnapshot;
    private BigDecimal totalPrice;
    private BigDecimal deliveryFee;
    private LocalDateTime createTime;
}
