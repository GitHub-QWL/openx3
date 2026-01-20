package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.common.utils.IdGenerator;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.employee.EmployeeCreateRequest;
import com.openx3.system.domain.dto.employee.EmployeeUpdateRequest;
import com.openx3.system.domain.dto.relation.EmployeePostBindRequest;
import com.openx3.system.entity.iam.SysAccount;
import com.openx3.system.entity.iam.SysDept;
import com.openx3.system.entity.iam.SysEmployee;
import com.openx3.system.entity.iam.SysPost;
import com.openx3.system.entity.iam.SysTenant;
import com.openx3.system.entity.relation.SysEmployeePost;
import com.openx3.system.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 员工管理服务（sys_employee）
 * 说明：员工是授权层业务身份（uid），通过部门/岗位间接获得角色与权限。
 */
@Service
@RequiredArgsConstructor
public class IamEmployeeService extends IamAdminService {

    private final SysEmployeeMapper employeeMapper;
    private final SysAccountMapper accountMapper;
    private final SysTenantMapper tenantMapper;
    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;
    private final SysEmployeePostMapper employeePostMapper;

    public IPage<SysEmployee> pageWithScope(PageRequest req) {
        checkAdmin();
        return employeeMapper.selectPageWithScope(new Page<>(req.getPageNo(), req.getPageSize()));
    }

    public SysEmployee get(String id) {
        checkAdmin();
        SysEmployee e = employeeMapper.selectById(id);
        if (e == null || e.getDelFlag() != null && e.getDelFlag() != 0) {
            throw new BusinessException(404, "员工不存在");
        }
        return e;
    }

    public String create(EmployeeCreateRequest req) {
        checkAdmin();

        SysAccount acc = accountMapper.selectById(req.getAccountId());
        if (acc == null || acc.getDelFlag() != null && acc.getDelFlag() != 0) {
            throw new BusinessException(400, "accountId不存在");
        }

        SysTenant t = tenantMapper.selectById(req.getTenantId());
        if (t == null || t.getDelFlag() != null && t.getDelFlag() != 0) {
            throw new BusinessException(400, "tenantId不存在");
        }

        if (StringUtils.hasText(req.getDeptId())) {
            SysDept d = deptMapper.selectById(req.getDeptId());
            if (d == null || d.getDelFlag() != null && d.getDelFlag() != 0) {
                throw new BusinessException(400, "deptId不存在");
            }
            if (!req.getTenantId().equals(d.getTenantId())) {
                throw new BusinessException(400, "deptId租户不一致");
            }
        }

        SysEmployee e = new SysEmployee();
        e.setId(IdGenerator.nextId());
        e.setAccountId(req.getAccountId());
        e.setTenantId(req.getTenantId());
        e.setDeptId(req.getDeptId());
        e.setEmpNo(req.getEmpNo());
        e.setRealName(req.getRealName());
        e.setIsMain(req.getIsMain());
        employeeMapper.insert(e);
        return e.getId();
    }

    public void update(EmployeeUpdateRequest req) {
        checkAdmin();

        SysEmployee e = employeeMapper.selectById(req.getId());
        if (e == null || e.getDelFlag() != null && e.getDelFlag() != 0) {
            throw new BusinessException(404, "员工不存在");
        }

        if (StringUtils.hasText(req.getDeptId())) {
            SysDept d = deptMapper.selectById(req.getDeptId());
            if (d == null || d.getDelFlag() != null && d.getDelFlag() != 0) {
                throw new BusinessException(400, "deptId不存在");
            }
            if (!e.getTenantId().equals(d.getTenantId())) {
                throw new BusinessException(400, "deptId租户不一致");
            }
        }

        e.setDeptId(req.getDeptId());
        e.setEmpNo(req.getEmpNo());
        e.setRealName(req.getRealName());
        e.setIsMain(req.getIsMain());
        employeeMapper.updateById(e);
    }

    public void delete(String id) {
        checkAdmin();
        employeeMapper.deleteById(id);
    }

    public void bindPosts(EmployeePostBindRequest req) {
        checkAdmin();

        SysEmployee e = employeeMapper.selectById(req.getEmployeeId());
        if (e == null || e.getDelFlag() != null && e.getDelFlag() != 0) {
            throw new BusinessException(404, "员工不存在");
        }

        // 校验岗位存在且租户一致
        if (req.getPostIds() != null && !req.getPostIds().isEmpty()) {
            List<SysPost> posts = postMapper.selectList(new LambdaQueryWrapper<SysPost>()
                    .in(SysPost::getId, req.getPostIds())
                    .eq(SysPost::getDelFlag, 0));
            Set<String> ok = new HashSet<>(posts.stream().map(SysPost::getId).toList());
            for (String pid : req.getPostIds()) {
                if (!ok.contains(pid)) throw new BusinessException(400, "postId不存在: " + pid);
            }
            for (SysPost p : posts) {
                if (!e.getTenantId().equals(p.getTenantId())) {
                    throw new BusinessException(400, "postId租户不一致: " + p.getId());
                }
            }
        }

        // 全量覆盖：先逻辑删除旧记录，再插入新记录
        employeePostMapper.delete(new LambdaQueryWrapper<SysEmployeePost>()
                .eq(SysEmployeePost::getEmployeeId, req.getEmployeeId()));

        if (req.getPostIds() == null) return;
        for (String postId : req.getPostIds()) {
            SysEmployeePost rel = new SysEmployeePost();
            rel.setEmployeeId(req.getEmployeeId());
            rel.setPostId(postId);
            employeePostMapper.insert(rel);
        }
    }
}

