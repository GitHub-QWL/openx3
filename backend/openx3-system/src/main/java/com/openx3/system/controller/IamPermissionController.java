package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.permission.PermissionCreateRequest;
import com.openx3.system.domain.dto.permission.PermissionUpdateRequest;
import com.openx3.system.entity.SysPermission;
import com.openx3.system.service.IamPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 权限码管理接口（sys_permission）
 * 说明：用于 API/运行时权限码（如 AUTH_{OBJECT}_{ACTION}）。
 */
@RestController
@RequestMapping("/api/iam/permissions")
@RequiredArgsConstructor
public class IamPermissionController {

    private final IamPermissionService service;

    @GetMapping("/page")
    public R<Page<SysPermission>> page(@Validated PageRequest req,
                                      @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    @GetMapping("/{id}")
    public R<SysPermission> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated PermissionCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated PermissionUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

