package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.common.utils.IdGenerator;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.dept.DeptCreateRequest;
import com.openx3.system.domain.dto.dept.DeptUpdateRequest;
import com.openx3.system.entity.iam.SysDept;
import com.openx3.system.entity.iam.SysEmployee;
import com.openx3.system.mapper.SysDeptMapper;
import com.openx3.system.mapper.SysEmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 部门管理服务（sys_dept）
 * - 创建时维护 tree_path（树路径）
 * - 删除前校验：无子部门、无员工归属
 */
@Service
@RequiredArgsConstructor
public class IamDeptService extends IamAdminService {

    private final SysDeptMapper deptMapper;
    private final SysEmployeeMapper employeeMapper;

    public Page<SysDept> page(PageRequest req, String tenantId, String keyword) {
        checkAdmin();
        Page<SysDept> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<SysDept> qw = new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDelFlag, 0);
        if (StringUtils.hasText(tenantId)) {
            qw.eq(SysDept::getTenantId, tenantId);
        }
        if (StringUtils.hasText(keyword)) {
            qw.like(SysDept::getDeptName, keyword);
        }
        qw.orderByAsc(SysDept::getSortNo);
        return deptMapper.selectPage(page, qw);
    }

    public SysDept get(String id) {
        checkAdmin();
        SysDept d = deptMapper.selectById(id);
        if (d == null || d.getDelFlag() != null && d.getDelFlag() != 0) {
            throw new BusinessException(404, "部门不存在");
        }
        return d;
    }

    public String create(DeptCreateRequest req) {
        checkAdmin();

        String id = IdGenerator.nextId();
        SysDept d = new SysDept();
        d.setId(id);
        d.setTenantId(req.getTenantId());
        d.setParentId(StringUtils.hasText(req.getParentId()) ? req.getParentId() : "0");
        d.setDeptName(req.getDeptName());
        d.setSortNo(req.getSortNo());
        d.setStatus(req.getStatus());

        String treePath;
        if ("0".equals(d.getParentId())) {
            treePath = id;
        } else {
            SysDept parent = deptMapper.selectById(d.getParentId());
            if (parent == null || parent.getDelFlag() != null && parent.getDelFlag() != 0) {
                throw new BusinessException(400, "父部门不存在");
            }
            if (!req.getTenantId().equals(parent.getTenantId())) {
                throw new BusinessException(400, "父部门租户不一致");
            }
            String parentPath = StringUtils.hasText(parent.getTreePath()) ? parent.getTreePath() : parent.getId();
            treePath = parentPath + "," + id;
        }
        d.setTreePath(treePath);

        deptMapper.insert(d);
        return d.getId();
    }

    public void update(DeptUpdateRequest req) {
        checkAdmin();
        SysDept d = deptMapper.selectById(req.getId());
        if (d == null || d.getDelFlag() != null && d.getDelFlag() != 0) {
            throw new BusinessException(404, "部门不存在");
        }
        d.setDeptName(req.getDeptName());
        d.setSortNo(req.getSortNo());
        d.setStatus(req.getStatus());
        deptMapper.updateById(d);
    }

    public void delete(String id) {
        checkAdmin();

        // 有子部门不允许删除
        long child = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, id)
                .eq(SysDept::getDelFlag, 0));
        if (child > 0) {
            throw new BusinessException(400, "存在子部门，禁止删除");
        }

        // 有员工归属不允许删除
        long emp = employeeMapper.selectCount(new LambdaQueryWrapper<SysEmployee>()
                .eq(SysEmployee::getDeptId, id)
                .eq(SysEmployee::getDelFlag, 0));
        if (emp > 0) {
            throw new BusinessException(400, "存在员工归属，禁止删除");
        }

        deptMapper.deleteById(id);
    }
}

