package com.example.coffee.service;

import com.example.coffee.common.PageResult;
import com.example.coffee.dto.request.RiderLocationReq;
import com.example.coffee.dto.response.RiderOrderResp;

import java.util.List;

public interface RiderService {

    PageResult<RiderOrderResp> getPendingOrders(int page, int size);

    void acceptOrder(Long riderId, Long orderId);

    void reportLocation(Long riderId, RiderLocationReq req);

    void pickupOrder(Long riderId, Long orderId);

    void deliverOrder(Long riderId, Long orderId);

    List<RiderOrderResp> getTasks(Long riderId);
}
