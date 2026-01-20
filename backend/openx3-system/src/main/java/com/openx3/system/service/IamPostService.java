package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.post.PostCreateRequest;
import com.openx3.system.domain.dto.post.PostUpdateRequest;
import com.openx3.system.entity.iam.SysPost;
import com.openx3.system.mapper.SysPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 岗位管理服务（sys_post）
 * - tenantId + postCode 唯一
 * - 岗位用于承载角色（sys_post_role）
 */
@Service
@RequiredArgsConstructor
public class IamPostService extends IamAdminService {

    private final SysPostMapper postMapper;

    public Page<SysPost> page(PageRequest req, String tenantId, String keyword) {
        checkAdmin();
        Page<SysPost> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<SysPost> qw = new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getDelFlag, 0);
        if (StringUtils.hasText(tenantId)) {
            qw.eq(SysPost::getTenantId, tenantId);
        }
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysPost::getPostCode, keyword).or().like(SysPost::getPostName, keyword));
        }
        return postMapper.selectPage(page, qw);
    }

    public SysPost get(String id) {
        checkAdmin();
        SysPost p = postMapper.selectById(id);
        if (p == null || p.getDelFlag() != null && p.getDelFlag() != 0) {
            throw new BusinessException(404, "岗位不存在");
        }
        return p;
    }

    public String create(PostCreateRequest req) {
        checkAdmin();

        long cnt = postMapper.selectCount(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getTenantId, req.getTenantId())
                .eq(SysPost::getPostCode, req.getPostCode())
                .eq(SysPost::getDelFlag, 0));
        if (cnt > 0) {
            throw new BusinessException(400, "postCode已存在");
        }

        SysPost p = new SysPost();
        p.setTenantId(req.getTenantId());
        p.setPostCode(req.getPostCode());
        p.setPostName(req.getPostName());
        p.setStatus(req.getStatus());
        postMapper.insert(p);
        return p.getId();
    }

    public void update(PostUpdateRequest req) {
        checkAdmin();
        SysPost p = postMapper.selectById(req.getId());
        if (p == null || p.getDelFlag() != null && p.getDelFlag() != 0) {
            throw new BusinessException(404, "岗位不存在");
        }
        p.setPostName(req.getPostName());
        p.setStatus(req.getStatus());
        postMapper.updateById(p);
    }

    public void delete(String id) {
        checkAdmin();
        postMapper.deleteById(id);
    }
}

