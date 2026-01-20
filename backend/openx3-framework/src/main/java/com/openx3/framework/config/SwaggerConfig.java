package com.openx3.framework.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "OpenX3 低代码平台 API",
                version = "V1.0.0",
                description = "基于 SpringBoot 3 + MyBatis Plus + Sa-Token 的企业级开发底座",
                contact = @Contact(name = "OpenX3 Team")
        ),
        // 全局安全要求：所有接口默认需要 satoken
        security = @SecurityRequirement(name = "Authorization")
)
// 定义安全鉴权方式：Header 中的 satoken 字段
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "Authorization",
        description = "请填入登录后获取的 Token 值"
)
public class SwaggerConfig {
    // SpringDoc 会自动扫描 @RestController 和 @Schema
}