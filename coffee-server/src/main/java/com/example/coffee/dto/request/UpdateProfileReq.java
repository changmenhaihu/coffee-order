package com.example.coffee.dto.request;

import lombok.Data;

@Data
public class UpdateProfileReq {

    private String nickname;

    private Integer gender;

    private String avatar;
}
