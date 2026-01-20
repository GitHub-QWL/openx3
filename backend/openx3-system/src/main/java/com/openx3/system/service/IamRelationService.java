package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.relation.DeptRoleBindRequest;
import com.openx3.system.domain.dto.relation.PostRoleBindRequest;
import com.openx3.system.domain.dto.relation.RoleMenuBindRequest;
import com.openx3.system.domain.dto.relation.RoleDeptBindRequest;
import com.openx3.system.domain.dto.relation.RolePermissionBindRequest;
import com.openx3.system.entity.SysRole;
import com.openx3.system.entity.SysMenu;
import com.openx3.system.entity.SysPermission;
import com.openx3.system.entity.iam.SysDept;
import com.openx3.system.entity.iam.SysPost;
import com.openx3.system.entity.relation.SysDeptRole;
import com.openx3.system.entity.relation.SysRoleMenu;
import com.openx3.system.entity.relation.SysRolePermission;
import com.openx3.system.entity.relation.SysPostRole;
import com.openx3.system.entity.relation.SysRoleDept;
import com.openx3.system.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 关系绑定服务（严格遵循权限设计）
 * - 用户/员工不得直接绑定角色
 * - 通过部门/岗位绑定角色
 * - 角色绑定菜单/权限码
 * - 角色自定义数据权限绑定部门
 */
@Service
@RequiredArgsConstructor
public class IamRelationService extends IamAdminService {

    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysPermissionMapper permissionMapper;

    private final SysDeptRoleMapper deptRoleMapper;
    private final SysPostRoleMapper postRoleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public void bindDeptRoles(DeptRoleBindRequest req) {
        checkAdmin();

        SysDept dept = deptMapper.selectById(req.getDeptId());
        if (dept == null || dept.getDelFlag() != null && dept.getDelFlag() != 0) {
            throw new BusinessException(404, "部门不存在");
        }

        validateRoles(req.getRoleIds());

        deptRoleMapper.delete(new LambdaQueryWrapper<SysDeptRole>()
                .eq(SysDeptRole::getDeptId, req.getDeptId()));

        for (String rid : req.getRoleIds()) {
            SysDeptRole rel = new SysDeptRole();
            rel.setDeptId(req.getDeptId());
            rel.setRoleId(rid);
            deptRoleMapper.insert(rel);
        }
    }

    public void bindPostRoles(PostRoleBindRequest req) {
        checkAdmin();

        SysPost post = postMapper.selectById(req.getPostId());
        if (post == null || post.getDelFlag() != null && post.getDelFlag() != 0) {
            throw new BusinessException(404, "岗位不存在");
        }

        validateRoles(req.getRoleIds());

        postRoleMapper.delete(new LambdaQueryWrapper<SysPostRole>()
                .eq(SysPostRole::getPostId, req.getPostId()));

        for (String rid : req.getRoleIds()) {
            SysPostRole rel = new SysPostRole();
            rel.setPostId(req.getPostId());
            rel.setRoleId(rid);
            postRoleMapper.insert(rel);
        }
    }

    public void bindRoleDepts(RoleDeptBindRequest req) {
        checkAdmin();

        SysRole role = roleMapper.selectById(req.getRoleId());
        if (role == null || role.getDelFlag() != null && role.getDelFlag() != 0) {
            throw new BusinessException(404, "角色不存在");
        }

        // 校验部门存在
        if (req.getDeptIds() != null && !req.getDeptIds().isEmpty()) {
            List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                    .in(SysDept::getId, req.getDeptIds())
                    .eq(SysDept::getDelFlag, 0));
            Set<String> ok = new HashSet<>(depts.stream().map(SysDept::getId).toList());
            for (String did : req.getDeptIds()) {
                if (!ok.contains(did)) throw new BusinessException(400, "deptId不存在: " + did);
            }
        }

        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, req.getRoleId()));

        for (String did : req.getDeptIds()) {
            SysRoleDept rel = new SysRoleDept();
            rel.setRoleId(req.getRoleId());
            rel.setDeptId(did);
            roleDeptMapper.insert(rel);
        }
    }

    public void bindRoleMenus(RoleMenuBindRequest req) {
        checkAdmin();

        SysRole role = roleMapper.selectById(req.getRoleId());
        if (role == null || role.getDelFlag() != null && role.getDelFlag() != 0) {
            throw new BusinessException(404, "角色不存在");
        }

        // 校验菜单存在
        if (req.getMenuIds() != null && !req.getMenuIds().isEmpty()) {
            List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getId, req.getMenuIds())
                    .eq(SysMenu::getDelFlag, 0));
            Set<String> ok = new HashSet<>(menus.stream().map(SysMenu::getId).toList());
            for (String mid : req.getMenuIds()) {
                if (!ok.contains(mid)) throw new BusinessException(400, "menuId不存在: " + mid);
            }
        }

        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, req.getRoleId()));

        for (String mid : req.getMenuIds()) {
            SysRoleMenu rel = new SysRoleMenu();
            rel.setRoleId(req.getRoleId());
            rel.setMenuId(mid);
            roleMenuMapper.insert(rel);
        }
    }

    public void bindRolePermissions(RolePermissionBindRequest req) {
        checkAdmin();

        SysRole role = roleMapper.selectById(req.getRoleId());
        if (role == null || role.getDelFlag() != null && role.getDelFlag() != 0) {
            throw new BusinessException(404, "角色不存在");
        }

        // 校验权限存在
        if (req.getPermissionIds() != null && !req.getPermissionIds().isEmpty()) {
            List<SysPermission> perms = permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                    .in(SysPermission::getId, req.getPermissionIds())
                    .eq(SysPermission::getDelFlag, 0));
            Set<String> ok = new HashSet<>(perms.stream().map(SysPermission::getId).toList());
            for (String pid : req.getPermissionIds()) {
                if (!ok.contains(pid)) throw new BusinessException(400, "permissionId不存在: " + pid);
            }
        }

        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, req.getRoleId()));

        for (String pid : req.getPermissionIds()) {
            SysRolePermission rel = new SysRolePermission();
            rel.setRoleId(req.getRoleId());
            rel.setPermissionId(pid);
            rolePermissionMapper.insert(rel);
        }
    }

    private void validateRoles(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getDelFlag, 0));
        Set<String> ok = new HashSet<>(roles.stream().map(SysRole::getId).toList());
        for (String rid : roleIds) {
            if (!ok.contains(rid)) throw new BusinessException(400, "roleId不存在: " + rid);
        }
    }
}

