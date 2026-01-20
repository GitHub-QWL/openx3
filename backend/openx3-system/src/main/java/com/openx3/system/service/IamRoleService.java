package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.role.RoleCreateRequest;
import com.openx3.system.domain.dto.role.RoleUpdateRequest;
import com.openx3.system.entity.SysRole;
import com.openx3.system.entity.relation.SysDeptRole;
import com.openx3.system.entity.relation.SysPostRole;
import com.openx3.system.entity.relation.SysRoleDept;
import com.openx3.system.entity.relation.SysRoleMenu;
import com.openx3.system.entity.relation.SysRolePermission;
import com.openx3.system.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 角色管理服务（sys_role）
 * 说明：角色是权限容器，包含 data_scope（数据范围）等关键字段。
 */
@Service
@RequiredArgsConstructor
public class IamRoleService extends IamAdminService {

    private final SysRoleMapper roleMapper;
    private final SysDeptRoleMapper deptRoleMapper;
    private final SysPostRoleMapper postRoleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public Page<SysRole> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<SysRole> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysRole::getCode, keyword).or().like(SysRole::getName, keyword));
        }
        return roleMapper.selectPage(page, qw);
    }

    public SysRole get(String id) {
        checkAdmin();
        SysRole r = roleMapper.selectById(id);
        if (r == null || r.getDelFlag() != null && r.getDelFlag() != 0) {
            throw new BusinessException(404, "角色不存在");
        }
        return r;
    }

    public String create(RoleCreateRequest req) {
        checkAdmin();
        String ds = normalizeDataScope(req.getDataScope());

        long cnt = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, req.getCode())
                .eq(SysRole::getDelFlag, 0));
        if (cnt > 0) throw new BusinessException(400, "roleCode已存在");

        SysRole r = new SysRole();
        r.setCode(req.getCode());
        r.setName(req.getName());
        r.setDataScope(ds);
        r.setDescription(req.getDescription());
        roleMapper.insert(r);
        return r.getId();
    }

    public void update(RoleUpdateRequest req) {
        checkAdmin();
        SysRole r = roleMapper.selectById(req.getId());
        if (r == null || r.getDelFlag() != null && r.getDelFlag() != 0) {
            throw new BusinessException(404, "角色不存在");
        }
        r.setName(req.getName());
        r.setDataScope(normalizeDataScope(req.getDataScope()));
        r.setDescription(req.getDescription());
        roleMapper.updateById(r);
    }

    public void delete(String id) {
        checkAdmin();

        // 有任何关联就不允许删除（避免 dangling 权限链）
        if (deptRoleMapper.selectCount(new LambdaQueryWrapper<SysDeptRole>().eq(SysDeptRole::getRoleId, id).eq(SysDeptRole::getDelFlag, 0)) > 0) {
            throw new BusinessException(400, "角色已绑定部门，禁止删除");
        }
        if (postRoleMapper.selectCount(new LambdaQueryWrapper<SysPostRole>().eq(SysPostRole::getRoleId, id).eq(SysPostRole::getDelFlag, 0)) > 0) {
            throw new BusinessException(400, "角色已绑定岗位，禁止删除");
        }
        if (roleDeptMapper.selectCount(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, id).eq(SysRoleDept::getDelFlag, 0)) > 0) {
            throw new BusinessException(400, "角色存在自定义部门权限，禁止删除");
        }
        if (roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id).eq(SysRoleMenu::getDelFlag, 0)) > 0) {
            throw new BusinessException(400, "角色已绑定菜单，禁止删除");
        }
        if (rolePermissionMapper.selectCount(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id).eq(SysRolePermission::getDelFlag, 0)) > 0) {
            throw new BusinessException(400, "角色已绑定权限，禁止删除");
        }

        roleMapper.deleteById(id);
    }

    private String normalizeDataScope(String ds) {
        if (!StringUtils.hasText(ds)) return "SELF";
        String v = ds.trim().toUpperCase(Locale.ROOT);
        return switch (v) {
            case "ALL", "DEPT_AND_CHILD", "DEPT", "SELF", "CUSTOM" -> v;
            default -> throw new BusinessException(400, "dataScope必须为 ALL/DEPT_AND_CHILD/DEPT/SELF/CUSTOM");
        };
    }
}

