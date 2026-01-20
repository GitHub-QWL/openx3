package com.openx3.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.employee.EmployeeCreateRequest;
import com.openx3.system.domain.dto.employee.EmployeeUpdateRequest;
import com.openx3.system.domain.dto.relation.EmployeePostBindRequest;
import com.openx3.system.entity.iam.SysEmployee;
import com.openx3.system.service.IamEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理接口（sys_employee）
 * 说明：员工是授权层业务身份（uid），通过部门/岗位间接获得角色与权限。
 */
@RestController
@RequestMapping("/api/iam/employees")
@RequiredArgsConstructor
public class IamEmployeeController {

    private final IamEmployeeService service;

    @GetMapping("/page")
    public R<IPage<SysEmployee>> page(@Validated PageRequest req) {
        return R.success(service.pageWithScope(req));
    }

    @GetMapping("/{id}")
    public R<SysEmployee> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated EmployeeCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated EmployeeUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }

    @PostMapping("/bind-posts")
    public R<String> bindPosts(@RequestBody @Validated EmployeePostBindRequest req) {
        service.bindPosts(req);
        return R.success("绑定成功");
    }
}

