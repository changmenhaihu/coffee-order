package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressResp {

    private String name;
    private String phone;
    private String address;
    private BigDecimal lng;
    private BigDecimal lat;
}
