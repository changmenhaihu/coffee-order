package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long storeId;

    private Long productId;

    /** 规格拼接（如 冰/少糖/大杯） */
    private String specInfo;

    /** 单价（含规格调整） */
    private BigDecimal price;

    private Integer quantity;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
