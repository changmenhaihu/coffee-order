package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RiderLocationResp {

    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDateTime updateTime;
}
