package com.example.coffee.enums;

import lombok.Getter;

@Getter
public enum MenuTypeEnum {
    DIRECTORY(1, "目录"),
    MENU(2, "菜单"),
    BUTTON(3, "按钮");

    private final int code;
    private final String desc;

    MenuTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
