package com.openx3.web;

import com.openx3.framework.config.banner.Openx3Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OpenX3 启动类
 * 聚合所有业务模块，启动应用
 */
@SpringBootApplication(scanBasePackages = "com.openx3")
@EnableScheduling
public class Openx3WebApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Openx3WebApplication.class);

        // ✅ 核心：设置自定义 Banner
        app.setBanner(new Openx3Banner());

        app.run(args);
    }
}