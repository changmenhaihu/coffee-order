package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    @TableField(select = false)
    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    private Integer gender;

    private BigDecimal balance;

    private String role;

    /** 所属门店ID，为空代表平台管理员/用户/骑手 */
    private Long storeId;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;
}
