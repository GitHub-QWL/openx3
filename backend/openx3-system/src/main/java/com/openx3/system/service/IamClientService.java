package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.common.utils.IdGenerator;
import com.openx3.common.utils.PasswordUtil;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.client.ClientCreateRequest;
import com.openx3.system.domain.dto.client.ClientRotateSecretRequest;
import com.openx3.system.domain.dto.client.ClientUpdateRequest;
import com.openx3.system.domain.vo.ClientCreateVO;
import com.openx3.system.entity.iam.SysClient;
import com.openx3.system.entity.iam.SysDept;
import com.openx3.system.entity.iam.SysEmployee;
import com.openx3.system.entity.iam.SysTenant;
import com.openx3.system.mapper.SysClientMapper;
import com.openx3.system.mapper.SysDeptMapper;
import com.openx3.system.mapper.SysEmployeeMapper;
import com.openx3.system.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 第三方系统对接（sys_client）管理服务
 * 对接文档：OpenX3 第三方系统对接标准方案（机器即用户 + Service Account）
 */
@Service
@RequiredArgsConstructor
public class IamClientService extends IamAdminService {

    private final SysClientMapper clientMapper;
    private final SysTenantMapper tenantMapper;
    private final SysDeptMapper deptMapper;
    private final SysEmployeeMapper employeeMapper;

    private final SecureRandom random = new SecureRandom();

    public Page<SysClient> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<SysClient> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<SysClient> qw = new LambdaQueryWrapper<SysClient>()
                .eq(SysClient::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysClient::getClientId, keyword).or().like(SysClient::getAppName, keyword));
        }
        return clientMapper.selectPage(page, qw);
    }

    public SysClient get(String id) {
        checkAdmin();
        SysClient c = clientMapper.selectById(id);
        if (c == null || c.getDelFlag() != null && c.getDelFlag() != 0) {
            throw new BusinessException(404, "客户端不存在");
        }
        return c;
    }

    public ClientCreateVO create(ClientCreateRequest req) {
        checkAdmin();

        SysTenant t = tenantMapper.selectById(req.getTenantId());
        if (t == null || t.getDelFlag() != null && t.getDelFlag() != 0) {
            throw new BusinessException(400, "tenantId不存在");
        }

        long cnt = clientMapper.selectCount(new LambdaQueryWrapper<SysClient>()
                .eq(SysClient::getClientId, req.getClientId())
                .eq(SysClient::getDelFlag, 0));
        if (cnt > 0) throw new BusinessException(400, "clientId已存在");

        String deptId = StringUtils.hasText(req.getDeptId()) ? req.getDeptId() : "dept-root";
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null || dept.getDelFlag() != null && dept.getDelFlag() != 0) {
            throw new BusinessException(400, "deptId不存在");
        }
        if (!req.getTenantId().equals(dept.getTenantId())) {
            throw new BusinessException(400, "deptId租户不一致");
        }

        // 1) 创建虚拟员工（Service Account）
        SysEmployee emp = new SysEmployee();
        emp.setId(IdGenerator.nextId());
        emp.setTenantId(req.getTenantId());
        emp.setDeptId(deptId);
        emp.setRealName(StringUtils.hasText(req.getAppName()) ? req.getAppName() : req.getClientId());
        emp.setIsMain(true);
        emp.setIsHuman(false);
        employeeMapper.insert(emp);

        // 2) 创建 client（secret 使用 BCrypt 存储）
        String plainSecret = StringUtils.hasText(req.getClientSecret()) ? req.getClientSecret() : generateSecret();

        SysClient c = new SysClient();
        c.setId(IdGenerator.nextId());
        c.setTenantId(req.getTenantId());
        c.setClientId(req.getClientId());
        c.setClientSecret(PasswordUtil.encode(plainSecret));
        c.setAppName(req.getAppName());
        c.setIpWhitelist(req.getIpWhitelist());
        c.setTokenValidity(req.getTokenValidity() == null ? 7200 : req.getTokenValidity());
        c.setRefEmployeeId(emp.getId());
        clientMapper.insert(c);

        ClientCreateVO vo = new ClientCreateVO();
        vo.setId(c.getId());
        vo.setTenantId(c.getTenantId());
        vo.setClientId(c.getClientId());
        vo.setClientSecret(plainSecret); // 只返回一次
        vo.setAppName(c.getAppName());
        vo.setRefEmployeeId(c.getRefEmployeeId());
        return vo;
    }

    public void update(ClientUpdateRequest req) {
        checkAdmin();
        SysClient c = clientMapper.selectById(req.getId());
        if (c == null || c.getDelFlag() != null && c.getDelFlag() != 0) {
            throw new BusinessException(404, "客户端不存在");
        }
        if (req.getAppName() != null) c.setAppName(req.getAppName());
        if (req.getIpWhitelist() != null) c.setIpWhitelist(req.getIpWhitelist());
        if (req.getTokenValidity() != null) c.setTokenValidity(req.getTokenValidity());
        if (req.getRefEmployeeId() != null) c.setRefEmployeeId(req.getRefEmployeeId());
        clientMapper.updateById(c);
    }

    public ClientCreateVO rotateSecret(ClientRotateSecretRequest req) {
        checkAdmin();
        SysClient c = clientMapper.selectById(req.getId());
        if (c == null || c.getDelFlag() != null && c.getDelFlag() != 0) {
            throw new BusinessException(404, "客户端不存在");
        }
        String plain = generateSecret();
        c.setClientSecret(PasswordUtil.encode(plain));
        clientMapper.updateById(c);

        ClientCreateVO vo = new ClientCreateVO();
        vo.setId(c.getId());
        vo.setTenantId(c.getTenantId());
        vo.setClientId(c.getClientId());
        vo.setClientSecret(plain);
        vo.setAppName(c.getAppName());
        vo.setRefEmployeeId(c.getRefEmployeeId());
        return vo;
    }

    public void delete(String id) {
        checkAdmin();
        clientMapper.deleteById(id);
    }

    private String generateSecret() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

