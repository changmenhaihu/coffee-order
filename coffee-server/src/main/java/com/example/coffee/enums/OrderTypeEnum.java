package com.example.coffee.enums;

import lombok.Getter;

@Getter
public enum OrderTypeEnum {
    TAKEAWAY("takeaway", "自取"),
    DELIVERY("delivery", "外送");

    private final String code;
    private final String desc;

    OrderTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
