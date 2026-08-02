package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    private Long orderId;

    private Long productId;

    private String productName;

    /** 规格拼接（如 冰/少糖/大杯） */
    private String specInfo;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal total;
}
