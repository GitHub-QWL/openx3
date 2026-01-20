package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.role.RoleCreateRequest;
import com.openx3.system.domain.dto.role.RoleUpdateRequest;
import com.openx3.system.entity.SysRole;
import com.openx3.system.service.IamRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理接口（sys_role）
 * 说明：权限只绑定到角色；用户/员工不得直接绑定权限。
 */
@RestController
@RequestMapping("/api/iam/roles")
@RequiredArgsConstructor
public class IamRoleController {

    private final IamRoleService service;

    @GetMapping("/page")
    public R<Page<SysRole>> page(@Validated PageRequest req,
                                @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    @GetMapping("/{id}")
    public R<SysRole> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated RoleCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated RoleUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

