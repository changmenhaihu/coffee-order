package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpecResp {

    private Long id;
    private Long productId;
    private String specName;
    private String specValue;
    private BigDecimal priceAdjust;
    private Integer status;
}
