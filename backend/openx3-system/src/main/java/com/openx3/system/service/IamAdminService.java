package com.openx3.system.service;

import cn.dev33.satoken.stp.StpUtil;

/**
 * IAM 管理端安全守卫（统一 admin 校验）
 */
public class IamAdminService {

    protected void checkAdmin() {
        StpUtil.checkLogin();
        StpUtil.checkRole("admin");
    }
}

