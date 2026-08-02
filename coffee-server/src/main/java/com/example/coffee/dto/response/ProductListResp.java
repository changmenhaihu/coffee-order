package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductListResp {

    private Long id;
    private Long storeId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String image;
    private BigDecimal price;
    private String description;
    private Integer isRecommend;
    private Integer sales;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer sortOrder;
    private List<SpecResp> specs;
}
