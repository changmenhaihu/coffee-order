package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feedback")
public class Feedback extends BaseEntity {

    private Long userId;

    private String content;

    private String images;

    private String contact;

    private Integer status;

    private String reply;
}
