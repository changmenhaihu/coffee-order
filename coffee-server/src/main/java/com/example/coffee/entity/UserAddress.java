package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_address")
public class UserAddress extends BaseEntity {

    private Long userId;

    private String name;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String detail;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer isDefault;
}
