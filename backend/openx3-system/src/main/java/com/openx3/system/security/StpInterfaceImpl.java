package com.openx3.system.security;

import cn.dev33.satoken.stp.StpInterface;
import com.openx3.system.domain.model.AuthTokenContext;
import com.openx3.system.service.IamRbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色获取实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final IamRbacService rbacService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        AuthTokenContext ctx = AuthContextHolder.getNullable();
        if (ctx == null) return List.of();
        return new ArrayList<>(rbacService.listAuthorities(ctx));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        AuthTokenContext ctx = AuthContextHolder.getNullable();
        if (ctx == null) return List.of();
        return new ArrayList<>(rbacService.listRoleCodes(ctx));
    }
}

