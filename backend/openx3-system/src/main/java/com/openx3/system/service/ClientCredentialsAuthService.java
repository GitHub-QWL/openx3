package com.openx3.system.service;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openx3.common.exception.BusinessException;
import com.openx3.common.utils.PasswordUtil;
import com.openx3.system.domain.model.AuthTokenContext;
import com.openx3.system.domain.model.BpContext;
import com.openx3.system.domain.vo.OAuthTokenVO;
import com.openx3.system.entity.SysRole;
import com.openx3.system.entity.iam.SysClient;
import com.openx3.system.entity.iam.SysEmployee;
import com.openx3.system.entity.iam.SysPost;
import com.openx3.system.entity.relation.SysRoleDept;
import com.openx3.system.mapper.SysClientMapper;
import com.openx3.system.mapper.SysEmployeeMapper;
import com.openx3.system.mapper.SysRoleDeptMapper;
import com.openx3.system.security.AuthContextHolder;
import com.openx3.system.security.client.IpWhitelistUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OAuth2 Client Credentials Grant（第三方系统换取 Token）
 * 对接文档：OpenX3 第三方系统对接标准方案
 */
@Service
@RequiredArgsConstructor
public class ClientCredentialsAuthService {

    private final SysClientMapper clientMapper;
    private final SysEmployeeMapper employeeMapper;
    private final IamRbacService rbacService;
    private final SysRoleDeptMapper roleDeptMapper;

    public OAuthTokenVO token(String grantType, String clientId, String clientSecret, String remoteIp) {
        if (!"client_credentials".equals(grantType)) {
            throw new BusinessException(400, "grant_type仅支持 client_credentials");
        }
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            throw new BusinessException(400, "client_id/client_secret不能为空");
        }

        SysClient client = clientMapper.selectOne(new LambdaQueryWrapper<SysClient>()
                .eq(SysClient::getClientId, clientId)
                .eq(SysClient::getDelFlag, 0));
        if (client == null) {
            throw new BusinessException(401, "client_id或client_secret错误");
        }

        if (!PasswordUtil.matches(clientSecret, client.getClientSecret())) {
            throw new BusinessException(401, "client_id或client_secret错误");
        }

        if (!IpWhitelistUtil.allow(client.getIpWhitelist(), remoteIp)) {
            throw new BusinessException(403, "IP不在白名单");
        }

        if (!StringUtils.hasText(client.getRefEmployeeId())) {
            throw new BusinessException(500, "客户端未关联虚拟员工");
        }

        SysEmployee emp = employeeMapper.selectById(client.getRefEmployeeId());
        if (emp == null || emp.getDelFlag() != null && emp.getDelFlag() != 0) {
            throw new BusinessException(500, "虚拟员工不存在");
        }

        // 登录：loginId 直接使用 clientId（sub）
        int timeout = client.getTokenValidity() == null ? 7200 : client.getTokenValidity();
        StpUtil.login(clientId, new SaLoginModel().setTimeout(timeout));

        AuthTokenContext ctx = buildClientTokenContext(client, emp);
        AuthContextHolder.set(ctx);

        OAuthTokenVO vo = new OAuthTokenVO();
        vo.setAccessToken(StpUtil.getTokenValue());
        vo.setExpiresIn(timeout);
        return vo;
    }

    private AuthTokenContext buildClientTokenContext(SysClient client, SysEmployee emp) {
        AuthTokenContext ctx = new AuthTokenContext();
        ctx.setSub(client.getClientId());
        ctx.setJti(UUID.randomUUID().toString());

        BpContext bp = new BpContext();
        bp.setUid(emp.getId());
        bp.setTid(client.getTenantId());
        bp.setDeptId(emp.getDeptId());
        List<SysPost> posts = rbacService.listPostsByEmployeeId(emp.getId());
        bp.setPosts(posts.stream().map(SysPost::getPostCode).collect(Collectors.toList()));
        ctx.setBpContext(bp);

        List<SysRole> roles = rbacService.listRoles(ctx);
        Set<String> roleCodes = roles.stream().map(SysRole::getCode).filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        ctx.setRoleCodes(roleCodes);

        ctx.setAuthorities(rbacService.listAuthorities(ctx));
        String dsScope = rbacService.calcMaxDataScope(ctx);
        ctx.setDsScope(dsScope);

        if ("CUSTOM".equalsIgnoreCase(dsScope) && !roles.isEmpty()) {
            Set<String> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toSet());
            List<SysRoleDept> rels = roleDeptMapper.selectList(new LambdaQueryWrapper<SysRoleDept>()
                    .in(SysRoleDept::getRoleId, roleIds)
                    .eq(SysRoleDept::getDelFlag, 0));
            for (SysRoleDept r : rels) {
                if (StringUtils.hasText(r.getDeptId())) {
                    ctx.getCustomDeptIds().add(r.getDeptId());
                }
            }
        }

        return ctx;
    }
}

