package com.openx3.job.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 任务初始化器
 * 应用启动时初始化所有启用的定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobInitializer implements CommandLineRunner {
    
//    private final JobService jobService;
    
    @Override
    public void run(String... args) {
        try {
            log.info("开始初始化定时任务...");
//            jobService.initJobs();
            log.info("定时任务初始化完成");
        } catch (Exception e) {
            log.error("初始化定时任务失败", e);
        }
    }
}
