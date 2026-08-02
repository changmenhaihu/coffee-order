package com.example.coffee.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartReq {

    @NotNull(message = "数量不能为空")
    private Integer quantity;
}
