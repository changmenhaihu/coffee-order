package com.example.coffee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResp {

    private String token;
    private String refreshToken;
    private Long expiresIn;
}
