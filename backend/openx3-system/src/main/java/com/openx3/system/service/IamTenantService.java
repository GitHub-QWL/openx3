package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.tenant.TenantCreateRequest;
import com.openx3.system.domain.dto.tenant.TenantUpdateRequest;
import com.openx3.system.entity.iam.SysTenant;
import com.openx3.system.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 租户管理服务（sys_tenant）
 */
@Service
@RequiredArgsConstructor
public class IamTenantService extends IamAdminService {

    private final SysTenantMapper tenantMapper;

    public Page<SysTenant> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<SysTenant> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<SysTenant> qw = new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.like(SysTenant::getName, keyword);
        }
        return tenantMapper.selectPage(page, qw);
    }

    public SysTenant get(String id) {
        checkAdmin();
        SysTenant t = tenantMapper.selectById(id);
        if (t == null || t.getDelFlag() != null && t.getDelFlag() != 0) {
            throw new BusinessException(404, "租户不存在");
        }
        return t;
    }

    public String create(TenantCreateRequest req) {
        checkAdmin();
        SysTenant t = new SysTenant();
        t.setName(req.getName());
        tenantMapper.insert(t);
        return t.getId();
    }

    public void update(TenantUpdateRequest req) {
        checkAdmin();
        SysTenant t = tenantMapper.selectById(req.getId());
        if (t == null || t.getDelFlag() != null && t.getDelFlag() != 0) {
            throw new BusinessException(404, "租户不存在");
        }
        t.setName(req.getName());
        tenantMapper.updateById(t);
    }

    public void delete(String id) {
        checkAdmin();
        tenantMapper.deleteById(id);
    }
}

