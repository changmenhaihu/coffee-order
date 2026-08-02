package com.example.coffee;



import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@SpringBootTest
public class MailTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void testSend() throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom("2930789735@qq.com");   
        helper.setTo("2878584501@qq.com");    
        helper.setSubject("测试邮件");
        helper.setText("Hello, 这是测试邮件");
        mailSender.send(msg);
        System.out.println("✅ 发送完成，请检查发件箱和垃圾箱");
    }
}