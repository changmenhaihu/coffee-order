package com.example.coffee.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.entity.*;
import com.example.coffee.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemController {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysOperLogMapper sysOperLogMapper;

    // ---- 菜单管理 ----

    @GetMapping("/menus")
    public Result<List<SysMenu>> menus() {
        List<SysMenu> all = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getOrderNum));
        return Result.success(buildMenuTree(all, 0L));
    }

    @PostMapping("/menus")
    public Result<Void> createMenu(@RequestBody SysMenu menu) {
        sysMenuMapper.insert(menu);
        return Result.success();
    }

    @PutMapping("/menus/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        sysMenuMapper.updateById(menu);
        return Result.success();
    }

    @DeleteMapping("/menus/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        sysMenuMapper.deleteById(id);
        return Result.success();
    }

    // ---- 角色菜单权限 ----

    @GetMapping("/roles/{role}/menus")
    public Result<List<Long>> roleMenus(@PathVariable String role) {
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRole, role));
        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).toList();
        return Result.success(menuIds);
    }

    @PutMapping("/roles/{role}/menus")
    @Transactional
    public Result<Void> updateRoleMenus(@PathVariable String role, @RequestBody List<Long> menuIds) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRole, role));
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRole(role);
            rm.setMenuId(menuId);
            sysRoleMenuMapper.insert(rm);
        }
        return Result.success();
    }

    // ---- 操作日志 ----

    @GetMapping("/logs/operation")
    public Result<PageResult<SysOperLog>> operationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer businessType) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<SysOperLog>();
        if (userId != null) wrapper.eq(SysOperLog::getUserId, userId);
        if (businessType != null) wrapper.eq(SysOperLog::getBusinessType, businessType);
        wrapper.orderByDesc(SysOperLog::getCreateTime);
        Page<SysOperLog> mpPage = new Page<>(page, size);
        Page<SysOperLog> result = sysOperLogMapper.selectPage(mpPage, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), result.getRecords()));
    }

    // ---- helper ----

    private List<SysMenu> buildMenuTree(List<SysMenu> all, Long parentId) {
        return all.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .peek(m -> m.setChildren(buildMenuTree(all, m.getId())))
                .toList();
    }
}
