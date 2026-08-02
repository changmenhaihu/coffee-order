package com.example.coffee.controller;

import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.dto.response.CategoryResp;
import com.example.coffee.dto.response.ProductDetailResp;
import com.example.coffee.dto.response.ProductListResp;
import com.example.coffee.service.ProductService;
import com.example.coffee.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/categories")
    public Result<List<CategoryResp>> categories(@RequestParam Long storeId) {
        return Result.success(productService.getCategories(storeId));
    }

    @GetMapping("/list")
    public Result<PageResult<ProductListResp>> list(
            @RequestParam Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(productService.getProductList(storeId, categoryId, keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<ProductDetailResp> detail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        return Result.success(productService.getProductDetail(id, userId));
    }
}
