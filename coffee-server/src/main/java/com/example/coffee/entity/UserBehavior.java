package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_behavior")
public class UserBehavior implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long productId;

    private Long storeId;

    private String type;

    private Integer count;

    private Integer score;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
