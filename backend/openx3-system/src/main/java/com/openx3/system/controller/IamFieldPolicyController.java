package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.fieldpolicy.FieldPolicyCreateRequest;
import com.openx3.system.domain.dto.fieldpolicy.FieldPolicyUpdateRequest;
import com.openx3.system.entity.SysFieldPolicy;
import com.openx3.system.service.IamFieldPolicyCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 字段权限策略管理接口（sys_field_policy）
 * 说明：用于字段级脱敏/隐藏/加密策略配置。
 */
@RestController
@RequestMapping("/api/iam/field-policies")
@RequiredArgsConstructor
public class IamFieldPolicyController {

    private final IamFieldPolicyCrudService service;

    @GetMapping("/page")
    public R<Page<SysFieldPolicy>> page(@Validated PageRequest req,
                                       @RequestParam(required = false) String roleId,
                                       @RequestParam(required = false) String resourceCode) {
        return R.success(service.page(req, roleId, resourceCode));
    }

    @GetMapping("/{id}")
    public R<SysFieldPolicy> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated FieldPolicyCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated FieldPolicyUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

