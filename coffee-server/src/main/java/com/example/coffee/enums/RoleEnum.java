package com.example.coffee.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    ADMIN("ADMIN", "平台管理员"),
    STORE_MANAGER("STORE_MANAGER", "商家"),
    RIDER("RIDER", "骑手"),
    USER("USER", "普通用户");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
