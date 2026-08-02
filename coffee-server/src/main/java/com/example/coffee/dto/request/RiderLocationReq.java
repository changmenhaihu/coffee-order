package com.example.coffee.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RiderLocationReq {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "经度不能为空")
    private BigDecimal lng;

    @NotNull(message = "纬度不能为空")
    private BigDecimal lat;

    private Float accuracy;
}
