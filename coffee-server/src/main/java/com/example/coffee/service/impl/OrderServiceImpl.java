package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.request.CancelOrderReq;
import com.example.coffee.dto.request.CreateOrderReq;
import com.example.coffee.dto.request.PayOrderReq;
import com.example.coffee.dto.response.*;
import com.example.coffee.entity.*;
import com.example.coffee.enums.OrderStatusEnum;
import com.example.coffee.mapper.*;
import com.example.coffee.service.OrderService;
import com.example.coffee.service.UserBehaviorService;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final StoreMapper storeMapper;
    private final SysUserMapper sysUserMapper;
    private final RiderLocationMapper riderLocationMapper;
    private final RedisUtil redisUtil;
    private final UserBehaviorService userBehaviorService;

    @Override
    @Transactional
    public OrderResp createOrder(Long userId, CreateOrderReq req) {
        List<Cart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        if (carts.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "购物车为空");
        }

        long storeCount = carts.stream().map(Cart::getStoreId).distinct().count();
        if (storeCount > 1) {
            throw new BusinessException(ResultCode.CONFLICT, "购物车存在多家门店商品，请先清空后重试");
        }
        if (!carts.get(0).getStoreId().equals(req.getStoreId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "门店不匹配");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (Cart cart : carts) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getStatus() == 0) {
                String name = product != null ? product.getName() : "未知商品";
                throw new BusinessException(ResultCode.CONFLICT, "商品[" + name + "]已下架");
            }

            OrderItem item = new OrderItem();
            item.setProductId(cart.getProductId());
            item.setProductName(product.getName());
            item.setSpecInfo(cart.getSpecInfo());
            item.setPrice(cart.getPrice());
            item.setQuantity(cart.getQuantity());
            BigDecimal subtotal = cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            item.setTotal(subtotal);
            orderItems.add(item);
            totalAmount = totalAmount.add(subtotal);
        }

        Store store = storeMapper.selectById(req.getStoreId());
        BigDecimal deliveryFee = req.getPickupType() != null && req.getPickupType() == 1 && store != null
                ? store.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.add(deliveryFee);

        String orderNo = generateOrderNo();

        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStoreId(req.getStoreId());
        order.setPickupType(req.getPickupType() != null ? req.getPickupType() : 0);
        order.setAddressId(req.getAddressId());
        order.setAddressSnapshot(req.getAddressSnapshot());
        order.setStatus(0); // 待支付，商家接单后进入制作中
        order.setTotalPrice(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setRemark(req.getRemark());
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setSales(product.getSales() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        // 清空购物车
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));

        for (OrderItem item : orderItems) {
            userBehaviorService.recordBehavior(userId, item.getProductId(), req.getStoreId(),
                    "order", null);
        }

        // 记录创建日志
        saveStatusLog(order.getId(), null, 0, userId, 1, "创建订单，等待支付/商家接单");

        OrderResp resp = new OrderResp();
        resp.setOrderId(order.getId());
        resp.setOrderNo(order.getOrderNo());
        resp.setStoreName(store != null ? store.getName() : "");
        resp.setPickupType(order.getPickupType());
        resp.setStatus(order.getStatus());
        resp.setStatusText(OrderStatusEnum.fromCode(order.getStatus()).getDesc());
        resp.setTotalPrice(order.getTotalPrice());
        resp.setDeliveryFee(order.getDeliveryFee());
        resp.setPayAmount(payAmount);
        resp.setCreateTime(order.getCreateTime());
        return resp;
    }

    @Override
    @Transactional
    public void payOrder(Long userId, Long orderId, PayOrderReq req) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单状态不允许支付");
        }
        // 检查支付超时（5分钟）
        if (order.getCreateTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
            order.setStatus(4);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("支付超时自动取消");
            orderMapper.updateById(order);
            saveStatusLog(orderId, 0, 4, 0L, 0, "支付超时自动取消");
            throw new BusinessException(ResultCode.CONFLICT, "订单已超时，请重新下单");
        }
        SysUser user = sysUserMapper.selectById(userId);
        BigDecimal payAmount = order.getTotalPrice().add(order.getDeliveryFee());
        if (user.getBalance().compareTo(payAmount) < 0) {
            throw new BusinessException(ResultCode.CONFLICT, "余额不足");
        }
        int updated = sysUserMapper.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
                        .ge(SysUser::getBalance, payAmount)
                        .setSql("balance = balance - " + payAmount));
        if (updated == 0) {
            throw new BusinessException(ResultCode.CONFLICT, "余额不足");
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        saveStatusLog(orderId, 0, 1, userId, 1, "支付成功，商家开始制作");
    }

    @Override
    public PageResult<OrderListResp> getOrderList(Long userId, String role, String status,
                                                    Integer pickupType, int page, int size) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>();
        if ("USER".equals(role)) {
            wrapper.eq(Orders::getUserId, userId);
        } else if ("RIDER".equals(role)) {
            wrapper.and(w -> w.eq(Orders::getStatus, 2)
                    .or().eq(Orders::getRiderId, userId));
        }

        if (status != null && !status.isBlank()) {
            String[] statusArr = status.split(",");
            wrapper.and(w -> {
                for (String s : statusArr) {
                    w.or().eq(Orders::getStatus, Integer.parseInt(s.trim()));
                }
            });
        }
        if (pickupType != null) {
            wrapper.eq(Orders::getPickupType, pickupType);
        }
        wrapper.orderByDesc(Orders::getCreateTime);

        Page<Orders> mpPage = new Page<>(page, size);
        Page<Orders> result = orderMapper.selectPage(mpPage, wrapper);

        List<OrderListResp> records = result.getRecords().stream()
                .map(this::toOrderListResp)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), records);
    }

    @Override
    public OrderDetailResp getOrderDetail(Long userId, Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }

        OrderDetailResp resp = new OrderDetailResp();
        fillOrderResp(resp, order);
        // 地址快照
        if (order.getAddressSnapshot() != null) {
            AddressResp ar = new AddressResp();
            ar.setAddress(order.getAddressSnapshot());
            resp.setAddress(ar);
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        resp.setItems(items.stream().map(i -> {
            OrderItemResp ir = new OrderItemResp();
            ir.setId(i.getId());
            ir.setProductId(i.getProductId());
            ir.setProductName(i.getProductName());
            ir.setSpecInfo(i.getSpecInfo());
            ir.setPrice(i.getPrice());
            ir.setQuantity(i.getQuantity());
            ir.setTotal(i.getTotal());
            return ir;
        }).collect(Collectors.toList()));

        // 状态时间线
        List<OrderStatusLog> logs = orderStatusLogMapper.selectList(
                new LambdaQueryWrapper<OrderStatusLog>()
                        .eq(OrderStatusLog::getOrderId, orderId)
                        .orderByAsc(OrderStatusLog::getCreateTime));
        resp.setStatusTimeline(logs.stream().map(l -> {
            OrderStatusTimelineResp tl = new OrderStatusTimelineResp();
            tl.setStatus(l.getToStatus());
            OrderStatusEnum statusEnum = OrderStatusEnum.fromCode(l.getToStatus());
            tl.setStatusText(statusEnum != null ? statusEnum.getDesc() : "未知");
            tl.setTime(l.getCreateTime());
            tl.setRemark(l.getRemark());
            return tl;
        }).collect(Collectors.toList()));

        // 骑手位置（配送中）
        if (order.getRiderId() != null) {
            RiderLocation location = riderLocationMapper.selectById(order.getRiderId());
            if (location != null) {
                RiderLocationResp rl = new RiderLocationResp();
                rl.setLongitude(location.getLongitude());
                rl.setLatitude(location.getLatitude());
                rl.setUpdateTime(location.getUpdateTime());
                resp.setRiderLocation(rl);
            }
        }

        return resp;
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId, CancelOrderReq req) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        // 只能取消待支付(0)或制作中(1)的订单
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许取消");
        }
        int oldStatus = order.getStatus();

        // 退回余额（已支付时）
        if (oldStatus == 1) {
            BigDecimal payAmount = order.getTotalPrice().add(order.getDeliveryFee());
            sysUserMapper.update(null,
                    new LambdaUpdateWrapper<SysUser>()
                            .eq(SysUser::getId, order.getUserId())
                            .setSql("balance = balance + " + payAmount));
        }

        order.setStatus(4);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(req.getReason());
        orderMapper.updateById(order);
        saveStatusLog(orderId, oldStatus, 4, userId, 1,
                req.getReason() != null ? req.getReason() : "用户取消");
    }

    @Override
    @Transactional
    public void confirmOrder(Long userId, Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        // 待取餐(2) → 已完成(3)
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许确认收货");
        }
        int oldStatus = order.getStatus();
        order.setStatus(3);
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
        saveStatusLog(orderId, oldStatus, 3, userId, 1, "用户确认取餐/收货");
    }

    // ========== 商家操作 ==========

    @Override
    @Transactional
    public void merchantAcceptOrder(Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许接单");
        }
        int oldStatus = order.getStatus();
        order.setStatus(1);
        order.setAcceptTime(LocalDateTime.now());
        orderMapper.updateById(order);
        saveStatusLog(orderId, oldStatus, 1, 1L, 3, "商家已接单，开始制作");
    }

    @Override
    @Transactional
    public void merchantCompleteOrder(Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许出餐");
        }
        int oldStatus = order.getStatus();
        order.setStatus(2);
        order.setPickupTime(LocalDateTime.now());
        orderMapper.updateById(order);
        saveStatusLog(orderId, oldStatus, 2, 1L, 3, "商家已出餐，等待取餐");
    }

    // ========== 骑手操作 ==========

    @Override
    @Transactional
    public void riderPickupOrder(Long riderId, Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null || !riderId.equals(order.getRiderId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此订单");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许取餐");
        }
        int oldStatus = order.getStatus();
        order.setStatus(3);
        order.setPickupTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
        saveStatusLog(orderId, oldStatus, 3, riderId, 2, "骑手已取餐并送达");
    }

    @Override
    @Transactional
    public void riderDeliverOrder(Long riderId, Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null || !riderId.equals(order.getRiderId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此订单");
        }
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许配送");
        }
        int oldStatus = order.getStatus();
        order.setStatus(3);
        order.setCompleteTime(LocalDateTime.now());
        order.setDeliverTime(LocalDateTime.now());
        orderMapper.updateById(order);
        saveStatusLog(orderId, oldStatus, 3, riderId, 2, "骑手确认送达");
    }

    // ========== 再来一单 ==========

    @Override
    @Transactional
    public void reorder(Long userId, Long orderId) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        if (items.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单无商品");
        }

        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));

        for (OrderItem item : items) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setStoreId(order.getStoreId());
            cart.setProductId(item.getProductId());
            cart.setSpecInfo(item.getSpecInfo());
            cart.setPrice(item.getPrice());
            cart.setQuantity(item.getQuantity());
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }
    }

    // ========== 商家端查询 ==========

    @Override
    public PageResult<OrderListResp> getStoreOrders(Long storeId, String status, int page, int size) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>();
        if (storeId != null) {
            wrapper.eq(Orders::getStoreId, storeId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(Orders::getStatus, Integer.parseInt(status.trim()));
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        Page<Orders> mpPage = new Page<>(page, size);
        Page<Orders> result = orderMapper.selectPage(mpPage, wrapper);
        List<OrderListResp> records = result.getRecords().stream()
                .map(this::toOrderListResp)
                .collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), records);
    }

    // ---- Helper methods ----

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 1000);
        return "CO" + datePart + String.format("%03d", random);
    }

    private void saveStatusLog(Long orderId, Integer fromStatus, Integer toStatus,
                                Long operatorId, int operatorType, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setOperatorType(operatorType);
        log.setRemark(remark);
        orderStatusLogMapper.insert(log);
    }

    private void fillOrderResp(OrderResp resp, Orders order) {
        resp.setOrderId(order.getId());
        resp.setOrderNo(order.getOrderNo());
        Store store = storeMapper.selectById(order.getStoreId());
        resp.setStoreName(store != null ? store.getName() : "");
        resp.setPickupType(order.getPickupType());
        resp.setStatus(order.getStatus());
        OrderStatusEnum statusEnum = OrderStatusEnum.fromCode(order.getStatus());
        resp.setStatusText(statusEnum != null ? statusEnum.getDesc() : "未知");
        resp.setTotalPrice(order.getTotalPrice());
        resp.setDeliveryFee(order.getDeliveryFee());
        resp.setPayAmount(order.getTotalPrice().add(order.getDeliveryFee()));
        resp.setCreateTime(order.getCreateTime());
    }

    private OrderListResp toOrderListResp(Orders order) {
        OrderListResp resp = new OrderListResp();
        fillOrderResp(resp, order);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        if (!items.isEmpty()) {
            OrderItem first = items.get(0);
            String desc = first.getProductName();
            if (first.getSpecInfo() != null && !first.getSpecInfo().isBlank()) {
                desc += "(" + first.getSpecInfo() + ")";
            }
            desc += " x" + first.getQuantity();
            if (items.size() > 1) {
                desc += "等" + items.size() + "件";
            }
            resp.setFirstItemDesc(desc);
        }
        return resp;
    }
}
