package com.example.coffee.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.common.ResultCode;
import com.example.coffee.entity.Orders;
import com.example.coffee.entity.SysUser;
import com.example.coffee.mapper.OrderMapper;
import com.example.coffee.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderMapper orderMapper;
    private final SysUserMapper sysUserMapper;

    @GetMapping
    public Result<PageResult<Orders>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String orderNo) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>();
        if (status != null) wrapper.eq(Orders::getStatus, status);
        if (type != null && !type.isBlank()) wrapper.eq(Orders::getPickupType, Integer.parseInt(type));
        if (storeId != null) wrapper.eq(Orders::getStoreId, storeId);
        if (orderNo != null && !orderNo.isBlank()) wrapper.eq(Orders::getOrderNo, orderNo);
        wrapper.orderByDesc(Orders::getCreateTime);
        Page<Orders> mpPage = new Page<>(page, size);
        Page<Orders> result = orderMapper.selectPage(mpPage, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<Orders> detail(@PathVariable Long id) {
        Orders order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return Result.success(order);
    }

    @PostMapping("/{id}/assign")
    @Transactional
    public Result<Void> assignRider(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long riderId = body.get("riderId");
        SysUser rider = sysUserMapper.selectById(riderId);
        if (rider == null || !"RIDER".equals(rider.getRole())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "骑手不存在");
        }
        Orders order = orderMapper.selectById(id);
        if (order == null || order.getStatus() == 4) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单当前状态不可指派");
        }
        order.setRiderId(riderId);
        orderMapper.updateById(order);
        return Result.success();
    }
}
