package com.example.coffee.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackResp {

    private Long id;
    private Long userId;
    private String nickname;
    private String content;
    private String images;
    private String contact;
    private Integer status;
    private String reply;
    private LocalDateTime createTime;
}
