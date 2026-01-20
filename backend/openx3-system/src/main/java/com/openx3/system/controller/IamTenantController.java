package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.tenant.TenantCreateRequest;
import com.openx3.system.domain.dto.tenant.TenantUpdateRequest;
import com.openx3.system.entity.iam.SysTenant;
import com.openx3.system.service.IamTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 租户管理接口（sys_tenant）
 * 说明：管理端接口默认要求 admin 角色（由 Service 层统一校验）。
 */
@RestController
@RequestMapping("/api/iam/tenants")
@RequiredArgsConstructor
public class IamTenantController {

    private final IamTenantService service;

    @GetMapping("/page")
    public R<Page<SysTenant>> page(@Validated PageRequest req,
                                  @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    @GetMapping("/{id}")
    public R<SysTenant> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated TenantCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated TenantUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

