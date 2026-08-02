package com.example.coffee.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginResp extends TokenResp {

    private UserInfoResp user;

    public LoginResp(String token, String refreshToken, Long expiresIn, UserInfoResp user) {
        super(token, refreshToken, expiresIn);
        this.user = user;
    }
}
