package com.example.coffee.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailResp extends OrderResp {

    private AddressResp address;
    private List<OrderItemResp> items;
    private List<OrderStatusTimelineResp> statusTimeline;
    private RiderLocationResp riderLocation;
}
