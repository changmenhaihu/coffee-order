package com.example.coffee.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.entity.Product;
import com.example.coffee.entity.ProductSpec;
import com.example.coffee.mapper.ProductMapper;
import com.example.coffee.mapper.ProductSpecMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductMapper productMapper;
    private final ProductSpecMapper productSpecMapper;

    @GetMapping
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>();
        if (storeId != null) wrapper.eq(Product::getStoreId, storeId);
        if (categoryId != null) wrapper.eq(Product::getCategoryId, categoryId);
        if (keyword != null && !keyword.isBlank()) wrapper.like(Product::getName, keyword);
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> mpPage = new Page<>(page, size);
        Page<Product> result = productMapper.selectPage(mpPage, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PostMapping
    @Transactional
    public Result<Void> create(@RequestBody Product product) {
        productMapper.insert(product);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productMapper.updateById(product);
        // 同步规格
        List<ProductSpec> specs = productSpecMapper.selectList(
                new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, id));
        // 规格变更由前端传 specs 列表统一处理（后续扩展）
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.success();
    }
}
