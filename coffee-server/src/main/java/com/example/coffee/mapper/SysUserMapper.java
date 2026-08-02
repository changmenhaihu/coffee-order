package com.example.coffee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coffee.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE id = #{id} AND deleted = 0")
    SysUser selectByIdWithPassword(@Param("id") Long id);
}
