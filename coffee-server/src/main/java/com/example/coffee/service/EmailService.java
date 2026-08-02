package com.example.coffee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;  

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")   // 注入发件人
    private String fromEmail;

    @Async
    public void sendCaptchaEmail(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);          // 必须设置发件人
            message.setTo(email);
            message.setSubject("【Coffee Order】邮箱验证码");
            message.setText("您的验证码是：" + code + "\n5分钟内有效。");
            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", email);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", email, e);
            throw new RuntimeException("邮件发送失败", e); // 抛出异常，让上层感知
        }
    }
}
