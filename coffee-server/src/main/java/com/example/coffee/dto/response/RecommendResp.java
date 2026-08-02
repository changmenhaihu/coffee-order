package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecommendResp {

    private Long productId;
    private Long storeId;
    private String productName;
    private String cover;
    private Double score;
    private String reason;
    private BigDecimal minPrice;
    private List<SpecResp> specs;
}
