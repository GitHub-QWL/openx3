package com.openx3.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * 使用方式：在方法上加 @Async("taskExecutor")
 */
@Configuration
@EnableAsync // 开启异步注解支持
public class ThreadPoolConfig {

    /**
     * 核心线程数 = CPU核数 + 1
     */
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;

    @Bean("taskExecutor") // Bean 名称，方便 @Async 指定
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 核心参数配置
        executor.setCorePoolSize(CORE_POOL_SIZE);     // 核心线程数
        executor.setMaxPoolSize(CORE_POOL_SIZE * 2);  // 最大线程数
        executor.setQueueCapacity(200);               // 队列容量 (超过核心数进队列)
        executor.setKeepAliveSeconds(60);             // 空闲线程存活时间

        // 2. 线程名称前缀 (方便排查日志，如 openx3-async-1)
        executor.setThreadNamePrefix("openx3-async-");

        // 3. 🛡️ 拒绝策略：CallerRunsPolicy (由调用者线程执行)
        // 作用：当线程池满了且队列也满了，不抛异常，而是让主线程自己去执行。
        // 优点：虽然会降低吞吐量，但能保证任务不丢失，且能减缓请求涌入。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 4. 优雅停机 (应用关闭时，等待任务执行完再销毁)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}