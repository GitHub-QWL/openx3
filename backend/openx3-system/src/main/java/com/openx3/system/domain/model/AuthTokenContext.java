package com.openx3.system.domain.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Token 内部上下文（用于服务端计算权限/数据范围/字段策略）
 */
@Data
public class AuthTokenContext implements Serializable {

    /**
     * Account ID（sub）
     */
    private String sub;

    /**
     * JWT ID（用于黑名单）
     */
    private String jti;

    /**
     * 业务上下文
     */
    private BpContext bpContext;

    /**
     * 角色编码快照（可选：便于前端展示/快速判断）
     */
    private Set<String> roleCodes = new LinkedHashSet<>();

    /**
     * 权限码快照（可选：避免每次都查询 DB；后端鉴权仍以实时计算为准）
     */
    private Set<String> authorities = new LinkedHashSet<>();

    /**
     * 数据权限范围（ALL/DEPT_AND_CHILD/DEPT/SELF/CUSTOM）
     */
    private String dsScope;

    /**
     * CUSTOM 数据权限时允许访问的部门集合（用于 SQL 拦截器）
     */
    private Set<String> customDeptIds = new LinkedHashSet<>();
}

