package com.example.coffee.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING_PAY(0, "待支付"),
    MAKING(1, "制作中"),
    READY(2, "待取餐"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatusEnum fromCode(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
