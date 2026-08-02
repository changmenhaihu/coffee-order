package com.example.coffee.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.entity.RiderLocation;
import com.example.coffee.entity.SysUser;
import com.example.coffee.mapper.RiderLocationMapper;
import com.example.coffee.mapper.SysUserMapper;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/riders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRiderController {

    private final SysUserMapper sysUserMapper;
    private final RiderLocationMapper riderLocationMapper;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "RIDER");
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> mpPage = new Page<>(page, size);
        Page<SysUser> result = sysUserMapper.selectPage(mpPage, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysUser rider) {
        rider.setRole("RIDER");
        rider.setPassword(passwordEncoder.encode(rider.getPassword()));
        rider.setStatus(1);
        sysUserMapper.insert(rider);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser rider) {
        rider.setId(id);
        sysUserMapper.updateById(rider);
        return Result.success();
    }

    @GetMapping("/{id}/track")
    public Result<RiderLocation> track(@PathVariable Long id) {
        RiderLocation cached = redisUtil.get("rider:location:" + id);
        if (cached != null) {
            return Result.success(cached);
        }
        RiderLocation location = riderLocationMapper.selectById(id);
        return Result.success(location);
    }
}
