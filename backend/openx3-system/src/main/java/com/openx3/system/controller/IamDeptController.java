package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.dept.DeptCreateRequest;
import com.openx3.system.domain.dto.dept.DeptUpdateRequest;
import com.openx3.system.entity.iam.SysDept;
import com.openx3.system.service.IamDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 部门管理接口（sys_dept）
 * 说明：部门树在创建时维护 tree_path；删除前校验无子节点与无员工归属。
 */
@RestController
@RequestMapping("/api/iam/depts")
@RequiredArgsConstructor
public class IamDeptController {

    private final IamDeptService service;

    @GetMapping("/page")
    public R<Page<SysDept>> page(@Validated PageRequest req,
                                @RequestParam(required = false) String tenantId,
                                @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, tenantId, keyword));
    }

    @GetMapping("/{id}")
    public R<SysDept> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated DeptCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated DeptUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

