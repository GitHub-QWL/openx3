package com.openx3.system.security;

import com.openx3.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token 黑名单拦截器：命中黑名单直接拒绝
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistInterceptor implements HandlerInterceptor {

    private final TokenBlacklistService blacklistService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractBearerToken(request);
        if (token != null && blacklistService.isBlacklisted(token)) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        return true;
    }

    private String extractBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null) return null;
        auth = auth.trim();
        if (auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return auth.substring(7).trim();
        }
        return null;
    }
}

