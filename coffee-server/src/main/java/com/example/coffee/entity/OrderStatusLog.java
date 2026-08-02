package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("order_status_log")
public class OrderStatusLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Integer fromStatus;

    private Integer toStatus;

    private Long operatorId;

    private Integer operatorType;

    private String remark;

    private LocalDateTime createTime;
}
