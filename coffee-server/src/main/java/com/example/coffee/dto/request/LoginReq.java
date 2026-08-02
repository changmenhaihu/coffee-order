package com.example.coffee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {

    @NotBlank(message = "账号不能为空")
    private String username;

    private String password;

    private String captcha;

    private String loginType;
}
