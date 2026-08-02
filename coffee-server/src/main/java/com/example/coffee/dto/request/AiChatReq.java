package com.example.coffee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiChatReq {

    @NotBlank(message = "消息不能为空")
    private String message;

    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    private List<Map<String, String>> history;
}
