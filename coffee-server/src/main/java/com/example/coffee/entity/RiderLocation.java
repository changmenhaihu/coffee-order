package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rider_location")
public class RiderLocation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long riderId;

    private Long orderId;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Float accuracy;

    private LocalDateTime updateTime;
}
