package com.example.coffee.common;

import lombok.Getter;

/**
 * 验证码发送频率限制异常
 * 用于返回剩余等待秒数给前端
 */
@Getter
public class CaptchaLimitException extends RuntimeException {

    private final int waitSeconds; // 剩余等待秒数

    public CaptchaLimitException(int waitSeconds) {
        super("发送过于频繁，请 " + waitSeconds + " 秒后再试");
        this.waitSeconds = waitSeconds;
    }
}