package com.openx3.system.controller;

import com.openx3.common.common.R;
import com.openx3.system.domain.dto.relation.DeptRoleBindRequest;
import com.openx3.system.domain.dto.relation.PostRoleBindRequest;
import com.openx3.system.domain.dto.relation.RoleDeptBindRequest;
import com.openx3.system.domain.dto.relation.RoleMenuBindRequest;
import com.openx3.system.domain.dto.relation.RolePermissionBindRequest;
import com.openx3.system.service.IamRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 关系表绑定接口（严格遵循：用户/员工不得直绑角色）
 */
@RestController
@RequestMapping("/api/iam/relations")
@RequiredArgsConstructor
public class IamRelationController {

    private final IamRelationService service;

    @PostMapping("/dept-roles")
    public R<String> bindDeptRoles(@RequestBody @Validated DeptRoleBindRequest req) {
        service.bindDeptRoles(req);
        return R.success("绑定成功");
    }

    @PostMapping("/post-roles")
    public R<String> bindPostRoles(@RequestBody @Validated PostRoleBindRequest req) {
        service.bindPostRoles(req);
        return R.success("绑定成功");
    }

    @PostMapping("/role-depts")
    public R<String> bindRoleDepts(@RequestBody @Validated RoleDeptBindRequest req) {
        service.bindRoleDepts(req);
        return R.success("绑定成功");
    }

    @PostMapping("/role-menus")
    public R<String> bindRoleMenus(@RequestBody @Validated RoleMenuBindRequest req) {
        service.bindRoleMenus(req);
        return R.success("绑定成功");
    }

    @PostMapping("/role-permissions")
    public R<String> bindRolePermissions(@RequestBody @Validated RolePermissionBindRequest req) {
        service.bindRolePermissions(req);
        return R.success("绑定成功");
    }
}

