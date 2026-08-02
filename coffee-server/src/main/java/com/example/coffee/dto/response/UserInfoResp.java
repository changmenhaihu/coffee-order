package com.example.coffee.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserInfoResp {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private BigDecimal balance;
    private String role;
    private Long storeId;
    private LocalDateTime createTime;
}
