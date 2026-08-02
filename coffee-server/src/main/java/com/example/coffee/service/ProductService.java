package com.example.coffee.service;

import com.example.coffee.common.PageResult;
import com.example.coffee.dto.response.CategoryResp;
import com.example.coffee.dto.response.ProductDetailResp;
import com.example.coffee.dto.response.ProductListResp;

import java.util.List;

public interface ProductService {

    List<CategoryResp> getCategories(Long storeId);

    PageResult<ProductListResp> getProductList(Long storeId, Long categoryId, String keyword, int page, int size);

    ProductDetailResp getProductDetail(Long productId, Long userId);
}
