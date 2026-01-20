package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.fieldpolicy.FieldPolicyCreateRequest;
import com.openx3.system.domain.dto.fieldpolicy.FieldPolicyUpdateRequest;
import com.openx3.system.entity.SysFieldPolicy;
import com.openx3.system.entity.SysRole;
import com.openx3.system.mapper.SysFieldPolicyMapper;
import com.openx3.system.mapper.SysRoleMapper;
import com.openx3.system.security.field.FieldPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 字段权限策略 CRUD 服务（sys_field_policy）
 * 说明：用于配置字段级的 HIDDEN/MASK/ENCRYPT 策略，并在修改后清空缓存。
 */
@Service
@RequiredArgsConstructor
public class IamFieldPolicyCrudService extends IamAdminService {

    private final SysFieldPolicyMapper mapper;
    private final SysRoleMapper roleMapper;
    private final FieldPolicyService fieldPolicyService;

    public Page<SysFieldPolicy> page(PageRequest req, String roleId, String resourceCode) {
        checkAdmin();
        Page<SysFieldPolicy> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<SysFieldPolicy> qw = new LambdaQueryWrapper<SysFieldPolicy>()
                .eq(SysFieldPolicy::getDelFlag, 0);
        if (StringUtils.hasText(roleId)) {
            qw.eq(SysFieldPolicy::getRoleId, roleId);
        }
        if (StringUtils.hasText(resourceCode)) {
            qw.eq(SysFieldPolicy::getResourceCode, resourceCode);
        }
        return mapper.selectPage(page, qw);
    }

    public SysFieldPolicy get(String id) {
        checkAdmin();
        SysFieldPolicy p = mapper.selectById(id);
        if (p == null || p.getDelFlag() != null && p.getDelFlag() != 0) {
            throw new BusinessException(404, "字段策略不存在");
        }
        return p;
    }

    public String create(FieldPolicyCreateRequest req) {
        checkAdmin();
        SysRole role = roleMapper.selectById(req.getRoleId());
        if (role == null || role.getDelFlag() != null && role.getDelFlag() != 0) {
            throw new BusinessException(400, "roleId不存在");
        }

        String policy = normalizePolicy(req.getPolicy());
        SysFieldPolicy p = new SysFieldPolicy();
        p.setRoleId(req.getRoleId());
        p.setResourceCode(req.getResourceCode());
        p.setFieldName(req.getFieldName());
        p.setPolicy(policy);
        p.setPolicyParam(req.getPolicyParam());
        mapper.insert(p);
        fieldPolicyService.clearCache();
        return p.getId();
    }

    public void update(FieldPolicyUpdateRequest req) {
        checkAdmin();
        SysFieldPolicy p = mapper.selectById(req.getId());
        if (p == null || p.getDelFlag() != null && p.getDelFlag() != 0) {
            throw new BusinessException(404, "字段策略不存在");
        }
        p.setPolicy(normalizePolicy(req.getPolicy()));
        p.setPolicyParam(req.getPolicyParam());
        mapper.updateById(p);
        fieldPolicyService.clearCache();
    }

    public void delete(String id) {
        checkAdmin();
        mapper.deleteById(id);
        fieldPolicyService.clearCache();
    }

    private String normalizePolicy(String policy) {
        if (policy == null) throw new BusinessException(400, "policy不能为空");
        String p = policy.trim().toUpperCase(Locale.ROOT);
        if (!("MASK".equals(p) || "HIDDEN".equals(p) || "ENCRYPT".equals(p))) {
            throw new BusinessException(400, "policy必须是 MASK/HIDDEN/ENCRYPT");
        }
        return p;
    }
}

