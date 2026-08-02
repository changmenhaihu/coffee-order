package com.example.coffee.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderReq {

    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    /** 取餐方式：0-自取 1-外卖 */
    @NotNull(message = "取餐方式不能为空")
    private Integer pickupType;

    private String remark;

    private Long addressId;

    private String addressSnapshot;
}
