package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Orders extends BaseEntity {

    private String orderNo;

    private Long userId;

    private Long storeId;

    private Long riderId;

    /** 取餐方式：0-自取 1-外卖 */
    private Integer pickupType;

    private Long addressId;

    /** 订单状态：0-待支付 1-制作中 2-待取餐 3-已完成 4-已取消 */
    private Integer status;

    private BigDecimal totalPrice;

    private BigDecimal deliveryFee;

    private BigDecimal discountAmount;

    private String addressSnapshot;

    private String remark;

    private LocalDateTime payTime;

    private LocalDateTime acceptTime;

    private LocalDateTime pickupTime;

    private LocalDateTime deliverTime;

    private LocalDateTime completeTime;

    private LocalDateTime cancelTime;

    private String cancelReason;
}
