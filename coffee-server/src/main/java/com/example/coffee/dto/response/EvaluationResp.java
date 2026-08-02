package com.example.coffee.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvaluationResp {

    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer score;
    private String content;
    private List<String> images;
    private LocalDateTime createTime;
}
