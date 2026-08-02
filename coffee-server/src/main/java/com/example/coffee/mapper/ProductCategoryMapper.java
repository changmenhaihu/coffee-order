package com.example.coffee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coffee.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
