package com.example.coffee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.response.OrderListResp;
import com.example.coffee.entity.Orders;
import com.example.coffee.entity.Product;
import com.example.coffee.entity.SysUser;
import com.example.coffee.mapper.OrderMapper;
import com.example.coffee.mapper.ProductMapper;
import com.example.coffee.mapper.SysUserMapper;
import com.example.coffee.service.OrderService;
import com.example.coffee.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家端接口（商家 / 平台管理员）
 * 商家根据当前用户 store_id 过滤本门店数据；平台管理员（store_id 为空）可查看全部门店数据。
 */
@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STORE_MANAGER','ADMIN')")
public class StoreMerchantController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final SysUserMapper sysUserMapper;

    /** 当前用户的门店ID；平台管理员为空 */
    private Long currentStoreId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getStoreId() : null;
    }

    private void checkStoreAccess(Long orderOrProductStoreId) {
        Long storeId = currentStoreId();
        if (storeId == null) {
            return; // 平台管理员可操作所有门店
        }
        if (orderOrProductStoreId == null || !storeId.equals(orderOrProductStoreId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作其他门店的数据");
        }
    }

    /** 查询本门店订单（可按状态过滤） */
    @GetMapping("/orders")
    public Result<PageResult<OrderListResp>> orders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long storeId = currentStoreId();
        return Result.success(orderService.getStoreOrders(storeId, status, page, size));
    }

    /** 接单：状态 0→1 */
    @PutMapping("/orders/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        Orders order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        checkStoreAccess(order.getStoreId());
        orderService.merchantAcceptOrder(id);
        return Result.success("接单成功，开始制作", null);
    }

    /** 出餐：状态 1→2 */
    @PutMapping("/orders/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        Orders order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        checkStoreAccess(order.getStoreId());
        orderService.merchantCompleteOrder(id);
        return Result.success("出餐成功，等待取餐", null);
    }

    /** 查看本门店商品 */
    @GetMapping("/products")
    public Result<List<Product>> products(@RequestParam(required = false) Long storeId) {
        Long myStoreId = currentStoreId();
        Long effectiveStoreId = myStoreId != null ? myStoreId : storeId;
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>();
        if (effectiveStoreId != null) {
            wrapper.eq(Product::getStoreId, effectiveStoreId);
        }
        wrapper.orderByAsc(Product::getSortOrder);
        return Result.success(productMapper.selectList(wrapper));
    }

    /** 编辑本门店商品（上下架 status、价格、名称、图片、描述、是否推荐） */
    @PutMapping("/products/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        checkStoreAccess(existing.getStoreId());
        product.setId(id);
        productMapper.updateById(product);
        return Result.success("商品已更新", null);
    }
}
