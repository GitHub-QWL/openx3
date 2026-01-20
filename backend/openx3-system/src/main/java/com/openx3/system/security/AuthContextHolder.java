package com.openx3.system.security;

import cn.dev33.satoken.stp.StpUtil;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.model.AuthTokenContext;

/**
 * 当前请求 Token 上下文读取工具
 */
public class AuthContextHolder {

    private static final String KEY = "openx3_auth_ctx";

    private AuthContextHolder() {
    }

    public static void set(AuthTokenContext ctx) {
        StpUtil.getTokenSession().set(KEY, ctx);
        // 额外写入基础字段，供 framework 层拦截器读取（避免跨模块依赖）
        if (ctx != null) {
            StpUtil.getTokenSession().set("openx3_sub", ctx.getSub());
            StpUtil.getTokenSession().set("openx3_jti", ctx.getJti());
            StpUtil.getTokenSession().set("openx3_ds_scope", ctx.getDsScope());
            StpUtil.getTokenSession().set("openx3_custom_dept_ids", ctx.getCustomDeptIds());
            if (ctx.getBpContext() != null) {
                StpUtil.getTokenSession().set("openx3_uid", ctx.getBpContext().getUid());
                StpUtil.getTokenSession().set("openx3_tid", ctx.getBpContext().getTid());
                StpUtil.getTokenSession().set("openx3_dept_id", ctx.getBpContext().getDeptId());
                StpUtil.getTokenSession().set("openx3_posts", ctx.getBpContext().getPosts());
            }
        }
    }

    public static AuthTokenContext getNullable() {
        Object obj = StpUtil.getTokenSession().get(KEY);
        if (obj instanceof AuthTokenContext) {
            return (AuthTokenContext) obj;
        }
        return null;
    }

    public static AuthTokenContext getRequired() {
        AuthTokenContext ctx = getNullable();
        if (ctx == null) {
            throw new BusinessException(401, "Token上下文缺失，请重新登录/选择身份");
        }
        return ctx;
    }
}

