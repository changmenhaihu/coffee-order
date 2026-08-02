package com.example.coffee.service;

import com.example.coffee.common.PageResult;
import com.example.coffee.dto.request.CancelOrderReq;
import com.example.coffee.dto.request.CreateOrderReq;
import com.example.coffee.dto.request.PayOrderReq;
import com.example.coffee.dto.response.OrderDetailResp;
import com.example.coffee.dto.response.OrderListResp;
import com.example.coffee.dto.response.OrderResp;

public interface OrderService {

    OrderResp createOrder(Long userId, CreateOrderReq req);

    void payOrder(Long userId, Long orderId, PayOrderReq req);

    PageResult<OrderListResp> getOrderList(Long userId, String role, String status, Integer pickupType, int page, int size);

    OrderDetailResp getOrderDetail(Long userId, Long orderId);

    void cancelOrder(Long userId, Long orderId, CancelOrderReq req);

    void confirmOrder(Long userId, Long orderId);

    void reorder(Long userId, Long orderId);

    // 商家操作
    void merchantAcceptOrder(Long orderId);
    void merchantCompleteOrder(Long orderId);

    // 商家端查询本门店订单
    PageResult<OrderListResp> getStoreOrders(Long storeId, String status, int page, int size);

    // 骑手操作
    void riderPickupOrder(Long riderId, Long orderId);
    void riderDeliverOrder(Long riderId, Long orderId);
}
