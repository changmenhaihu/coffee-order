package com.example.coffee.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private Long parentId;

    private String name;

    private String path;

    private String component;

    private String title;

    private String icon;

    private Integer orderNum;

    private Integer menuType;

    private String perms;

    private Integer status;

    @TableField(exist = false)
    private java.util.List<SysMenu> children;
}
