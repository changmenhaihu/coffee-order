package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("evaluation")
public class Evaluation extends BaseEntity {

    private Long orderId;

    private Long userId;

    private Long storeId;

    private Long productId;

    private Integer score;

    private String content;

    private String images;

    private Integer isAnonymous;

    private String reply;

    private LocalDateTime replyTime;

    private Integer status;
}
