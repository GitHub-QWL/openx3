package com.openx3.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // 1. Knife4j / Swagger 文档配置 (关键！否则 doc.html 报 404)
//        // 这些文件位于 jar 包内部的 META-INF/resources 下
//        registry.addResourceHandler("doc.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//
//        // 2. 静态资源配置 (Favicon 等)
//        // 告诉 Spring 去 src/main/resources/static/ 下找文件
//        registry.addResourceHandler("/**")
//                .addResourceLocations("classpath:/static/");
//    }
}
