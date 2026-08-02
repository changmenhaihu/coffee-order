package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private Long storeId;

    private Long categoryId;

    private String name;

    private String image;

    private BigDecimal price;

    private String description;

    private Integer isRecommend;

    private Integer status;

    private Integer sales;

    private BigDecimal rating;

    private Integer ratingCount;

    private Integer sortOrder;
}
