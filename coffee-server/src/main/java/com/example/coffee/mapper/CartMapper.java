package com.example.coffee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coffee.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
