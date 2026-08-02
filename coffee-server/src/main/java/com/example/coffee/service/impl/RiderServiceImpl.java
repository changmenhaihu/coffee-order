package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.request.RiderLocationReq;
import com.example.coffee.dto.response.RiderOrderResp;
import com.example.coffee.entity.*;
import com.example.coffee.mapper.*;
import com.example.coffee.service.RiderService;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiderServiceImpl implements RiderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final RiderLocationMapper riderLocationMapper;
    private final StoreMapper storeMapper;
    private final SysUserMapper sysUserMapper;
    private final RedisUtil redisUtil;

    @Override
    public PageResult<RiderOrderResp> getPendingOrders(int page, int size) {
        // 骑手可接的订单：外卖(pickup_type=1)、待取餐(2)、未分配骑手
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getPickupType, 1)
                .eq(Orders::getStatus, 2)
                .isNull(Orders::getRiderId)
                .orderByDesc(Orders::getCreateTime);

        Page<Orders> mpPage = new Page<>(page, size);
        Page<Orders> result = orderMapper.selectPage(mpPage, wrapper);

        List<RiderOrderResp> records = result.getRecords().stream()
                .map(this::toRiderOrderResp)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), records);
    }

    @Override
    @Transactional
    public void acceptOrder(Long riderId, Long orderId) {
        String lockKey = "order:accept:" + orderId;
        if (!redisUtil.tryLock(lockKey, 5, TimeUnit.SECONDS)) {
            throw new BusinessException(ResultCode.CONFLICT, "订单已被其他骑手抢走");
        }
        try {
            Orders order = orderMapper.selectById(orderId);
            if (order == null || order.getPickupType() == null || order.getPickupType() != 1
                    || order.getStatus() != 2 || order.getRiderId() != null) {
                throw new BusinessException(ResultCode.CONFLICT, "订单已被其他骑手抢走");
            }
            order.setRiderId(riderId);
            orderMapper.updateById(order);

            OrderStatusLog log = new OrderStatusLog();
            log.setOrderId(orderId);
            log.setFromStatus(2);
            log.setToStatus(2);
            log.setOperatorId(riderId);
            log.setOperatorType(2);
            log.setRemark("骑手已接配送单，等待取餐");
            orderStatusLogMapper.insert(log);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    public void reportLocation(Long riderId, RiderLocationReq req) {
        RiderLocation existing = riderLocationMapper.selectById(riderId);
        RiderLocation location = existing != null ? existing : new RiderLocation();
        if (existing == null) {
            location.setRiderId(riderId);
        }
        location.setOrderId(req.getOrderId());
        location.setLongitude(req.getLng());
        location.setLatitude(req.getLat());
        location.setAccuracy(req.getAccuracy());
        location.setUpdateTime(LocalDateTime.now());
        if (existing != null) {
            riderLocationMapper.updateById(location);
        } else {
            riderLocationMapper.insert(location);
        }
        redisUtil.set("rider:location:" + riderId, location, 120, TimeUnit.SECONDS);
    }

    @Override
    @Transactional
    public void pickupOrder(Long riderId, Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null || !riderId.equals(order.getRiderId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此订单");
        }
        // 待取餐(2) → 已完成(3)
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许取餐");
        }
        int oldStatus = order.getStatus();
        order.setStatus(3);
        order.setPickupTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(oldStatus);
        log.setToStatus(3);
        log.setOperatorId(riderId);
        log.setOperatorType(2);
        log.setRemark("骑手已取餐并送达");
        orderStatusLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void deliverOrder(Long riderId, Long orderId) {
        // 外卖配送与取餐合并为同一动作：2 → 3 已完成
        pickupOrder(riderId, orderId);
    }

    @Override
    public List<RiderOrderResp> getTasks(Long riderId) {
        List<Orders> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getRiderId, riderId)
                        .in(Orders::getStatus, 1, 2, 3)
                        .orderByDesc(Orders::getCreateTime));
        return orders.stream().map(this::toRiderOrderResp).collect(Collectors.toList());
    }

    private RiderOrderResp toRiderOrderResp(Orders order) {
        RiderOrderResp resp = new RiderOrderResp();
        resp.setOrderId(order.getId());
        resp.setOrderNo(order.getOrderNo());
        Store store = storeMapper.selectById(order.getStoreId());
        resp.setStoreName(store != null ? store.getName() : "");
        resp.setStoreAddress(store != null ? store.getAddress() : "");
        resp.setPickupType(order.getPickupType());
        resp.setAddressSnapshot(order.getAddressSnapshot());
        resp.setTotalPrice(order.getTotalPrice().add(order.getDeliveryFee()));
        resp.setDeliveryFee(order.getDeliveryFee());
        resp.setCreateTime(order.getCreateTime());
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        if (!items.isEmpty()) {
            OrderItem first = items.get(0);
            resp.setFirstItemName(first.getProductName()
                    + (first.getSpecInfo() != null && !first.getSpecInfo().isBlank()
                        ? "(" + first.getSpecInfo() + ")" : ""));
        }
        return resp;
    }
}
