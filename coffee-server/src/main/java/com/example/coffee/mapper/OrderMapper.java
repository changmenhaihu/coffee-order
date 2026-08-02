package com.example.coffee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coffee.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
