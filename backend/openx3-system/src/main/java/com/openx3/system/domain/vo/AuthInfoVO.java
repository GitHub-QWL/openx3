package com.openx3.system.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 当前登录态信息（用户 + 角色 + 权限 + 菜单）
 */
@Data
public class AuthInfoVO {

    private AccountVO account;

    /**
     * 当前业务身份（employee）
     */
    private EmployeeContextVO employee;

    /**
     * 角色编码列表（如：admin、dev）
     */
    private Set<String> roles;

    /**
     * 权限码列表（如：sys:user:list、AUTH_SYS_USER_QUERY）
     */
    private Set<String> permissions;

    /**
     * 数据权限范围（ALL/DEPT_AND_CHILD/DEPT/SELF/CUSTOM）
     */
    private String dsScope;

    /**
     * 菜单树
     */
    private List<MenuVO> menus;
}

