package com.openx3.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API 文档配置 (基于 OpenAPI 3 / Swagger 3)
 * 访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OpenX3 平台接口文档")
                        .version("1.0.0")
                        .description("基于 Spring Boot 3 + Modular Monolith 架构的工业级开发平台")
                        .contact(new Contact()
                                .name("OpenX3 Team")
                                .url("https://www.openx3.com")
                                .email("dev@openx3.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}