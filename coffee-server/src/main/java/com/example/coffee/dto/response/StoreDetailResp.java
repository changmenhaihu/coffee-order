package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
public class StoreDetailResp {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String image;
    private String businessHours;
    private LocalTime openTime;
    private LocalTime closeTime;
    private BigDecimal minDeliveryAmount;
    private BigDecimal deliveryFee;
    private Integer status;
    private List<CategoryWithProductsResp> categories;
}
