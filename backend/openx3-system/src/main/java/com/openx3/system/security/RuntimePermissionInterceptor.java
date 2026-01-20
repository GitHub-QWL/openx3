package com.openx3.system.security;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;

/**
 * 运行时接口权限拦截：
 * /api/runtime/{objectCode}/{action} -> AUTH_{OBJECTCODE}_{ACTION}
 */
@Component
public class RuntimePermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1) 必须先登录
        StpUtil.checkLogin();

        // 2) 超级管理员放行
        if (StpUtil.hasRole("admin")) {
            return true;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        // uri: /api/runtime/{objectCode}/{action}
        String[] parts = uri.split("/");
        if (parts.length < 5) {
            return true; // 非标准路由，交给下游处理
        }

        String objectCode = parts[3];
        String action = parts[4];

        String permAction = mapAction(action);
        if (permAction == null) {
            permAction = action.toUpperCase(Locale.ROOT);
        }

        String permissionCode = "AUTH_" + objectCode.toUpperCase(Locale.ROOT) + "_" + permAction;
        StpUtil.checkPermission(permissionCode);
        return true;
    }

    private String mapAction(String action) {
        if (action == null) return null;
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "list", "query" -> "QUERY";
            case "save", "create", "update" -> "SAVE";
            case "delete", "remove" -> "DELETE";
            default -> null;
        };
    }
}

