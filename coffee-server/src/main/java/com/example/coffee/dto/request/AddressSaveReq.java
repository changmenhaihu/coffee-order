package com.example.coffee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressSaveReq {

    @NotBlank(message = "收货人姓名不能为空")
    private String name;

    @NotBlank(message = "收货人手机号不能为空")
    private String phone;

    private String province;

    private String city;

    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer isDefault;
}
