package com.example.coffee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coffee.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
