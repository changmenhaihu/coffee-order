package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store")
public class Store extends BaseEntity {

    private String name;

    private String address;

    private String phone;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String image;

    private String businessHours;

    private LocalTime openTime;

    private LocalTime closeTime;

    private BigDecimal minDeliveryAmount;

    private BigDecimal deliveryFee;

    private Integer status;
}
