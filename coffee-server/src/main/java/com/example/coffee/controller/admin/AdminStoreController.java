package com.example.coffee.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.entity.Store;
import com.example.coffee.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStoreController {

    private final StoreMapper storeMapper;

    @GetMapping
    public Result<PageResult<Store>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<Store>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Store::getName, name);
        }
        wrapper.orderByDesc(Store::getCreateTime);
        Page<Store> mpPage = new Page<>(page, size);
        Page<Store> result = storeMapper.selectPage(mpPage, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Store store) {
        storeMapper.insert(store);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Store store) {
        store.setId(id);
        storeMapper.updateById(store);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        storeMapper.deleteById(id);
        return Result.success();
    }
}
