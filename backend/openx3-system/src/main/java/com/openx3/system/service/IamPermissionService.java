package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.permission.PermissionCreateRequest;
import com.openx3.system.domain.dto.permission.PermissionUpdateRequest;
import com.openx3.system.entity.SysPermission;
import com.openx3.system.entity.relation.SysRolePermission;
import com.openx3.system.mapper.SysPermissionMapper;
import com.openx3.system.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 权限码管理服务（sys_permission）
 * 说明：用于 API/运行时权限码配置与维护。
 */
@Service
@RequiredArgsConstructor
public class IamPermissionService extends IamAdminService {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public Page<SysPermission> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<SysPermission> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<SysPermission> qw = new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysPermission::getCode, keyword).or().like(SysPermission::getName, keyword).or().like(SysPermission::getObjectCode, keyword));
        }
        return permissionMapper.selectPage(page, qw);
    }

    public SysPermission get(String id) {
        checkAdmin();
        SysPermission p = permissionMapper.selectById(id);
        if (p == null || p.getDelFlag() != null && p.getDelFlag() != 0) {
            throw new BusinessException(404, "权限不存在");
        }
        return p;
    }

    public String create(PermissionCreateRequest req) {
        checkAdmin();
        long cnt = permissionMapper.selectCount(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getCode, req.getCode())
                .eq(SysPermission::getDelFlag, 0));
        if (cnt > 0) throw new BusinessException(400, "permission code已存在");

        SysPermission p = new SysPermission();
        p.setCode(req.getCode());
        p.setName(req.getName());
        p.setObjectCode(req.getObjectCode());
        p.setAction(req.getAction());
        p.setDescription(req.getDescription());
        permissionMapper.insert(p);
        return p.getId();
    }

    public void update(PermissionUpdateRequest req) {
        checkAdmin();
        SysPermission p = permissionMapper.selectById(req.getId());
        if (p == null || p.getDelFlag() != null && p.getDelFlag() != 0) {
            throw new BusinessException(404, "权限不存在");
        }
        p.setName(req.getName());
        p.setObjectCode(req.getObjectCode());
        p.setAction(req.getAction());
        p.setDescription(req.getDescription());
        permissionMapper.updateById(p);
    }

    public void delete(String id) {
        checkAdmin();

        long used = rolePermissionMapper.selectCount(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id)
                .eq(SysRolePermission::getDelFlag, 0));
        if (used > 0) {
            throw new BusinessException(400, "权限已被角色引用，禁止删除");
        }

        permissionMapper.deleteById(id);
    }
}

