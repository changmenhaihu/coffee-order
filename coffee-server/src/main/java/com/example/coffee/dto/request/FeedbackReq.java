package com.example.coffee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeedbackReq {

    @NotBlank(message = "反馈内容不能为空")
    private String content;

    private String images;

    private String contact;
}
