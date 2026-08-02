package com.example.coffee.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddCartReq {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    /** 选中的规格ID列表（如 温度=冰、糖度=少糖、杯型=大杯 各选一个） */
    @NotEmpty(message = "请选择规格")
    private List<Long> specIds;

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;
}
