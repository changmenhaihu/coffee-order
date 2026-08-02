package com.example.coffee.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderStatusTimelineResp {

    private Integer status;
    private String statusText;
    private LocalDateTime time;
    private String remark;
}
