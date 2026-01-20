package com.openx3.system.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 安全拦截配置
 */
@Configuration
@RequiredArgsConstructor
public class SecurityWebMvcConfig implements WebMvcConfigurer {

    private final RuntimePermissionInterceptor runtimePermissionInterceptor;
    private final TokenBlacklistInterceptor tokenBlacklistInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 全局黑名单校验（除登录/选身份）
        registry.addInterceptor(tokenBlacklistInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/select");

        registry.addInterceptor(runtimePermissionInterceptor)
                .addPathPatterns("/api/runtime/**");
    }
}

