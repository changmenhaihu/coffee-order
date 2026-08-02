package com.example.coffee.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderListResp extends OrderResp {

    private String firstItemDesc;
}
