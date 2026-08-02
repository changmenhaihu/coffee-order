package com.example.coffee.enums;

import lombok.Getter;

@Getter
public enum BehaviorTypeEnum {
    VIEW("view", "浏览", 1),
    CART("cart", "加购", 2),
    ORDER("order", "下单", 3),
    FAVORITE("favorite", "收藏", 5);

    private final String code;
    private final String desc;
    private final int weight;

    BehaviorTypeEnum(String code, String desc, int weight) {
        this.code = code;
        this.desc = desc;
        this.weight = weight;
    }
}
