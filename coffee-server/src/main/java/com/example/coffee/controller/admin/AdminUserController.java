package com.example.coffee.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.common.ResultCode;
import com.example.coffee.entity.SysUser;
import com.example.coffee.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final SysUserMapper sysUserMapper;

    @GetMapping
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(SysUser::getRole, role);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> mpPage = new Page<>(page, size);
        Page<SysUser> result = sysUserMapper.selectPage(mpPage, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        sysUserMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/{id}/recharge")
    @Transactional
    public Result<Void> recharge(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "充值金额必须大于0");
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setBalance(user.getBalance().add(amount));
        sysUserMapper.updateById(user);
        return Result.success("充值成功", null);
    }
}
