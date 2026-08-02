package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_spec")
public class ProductSpec extends BaseEntity {

    private Long productId;

    /** 规格维度（温度/糖度/杯型） */
    private String specName;

    /** 具体选项值（热/冰/正常糖/中杯） */
    private String specValue;

    /** 价格调整（可为0/正/负） */
    private BigDecimal priceAdjust;

    private Integer status;
}
