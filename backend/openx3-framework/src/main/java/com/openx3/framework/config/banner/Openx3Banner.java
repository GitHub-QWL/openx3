package com.openx3.framework.config.banner;

import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全动态 Banner 生成器
 * 自动读取 Classpath 下各框架的版本号
 */
public class Openx3Banner implements Banner {

    // ASCII Art 字符画 (不用担心转义问题，这是 Java 字符串)
    private static final String BANNER_ART =
            "\n" +
                    "   ____   ____  _____ _   _ __  __ _____\n" +
                    "  / __ \\ |  _ \\| ____| \\ | |\\ \\/ /|___ /\n" +
                    " | |  | || |_) |  _| |  \\| | \\  /   |_ \\\n" +
                    " | |__| ||  __/| |___| |\\  | /  \\  ___) |\n" +
                    "  \\____/ |_|   |_____|_| \\_|/_/\\_\\|____/\n";

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        // 1. 打印 ASCII Art (亮蓝色)
        out.println(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, BANNER_ART));

        // 2. 准备要展示的版本信息 (名称 -> 核心类)
        // 技巧：只要能找到这个框架的一个类，就能找到它的包，从而找到版本
        Map<String, String> versions = new LinkedHashMap<>();

        // A. 当前应用版本 (读取 build.gradle 打包进 Manifest 的版本)
        String appVersion = (sourceClass != null ? sourceClass.getPackage().getImplementationVersion() : "Dev");
        versions.put("OpenX3 Platform", appVersion != null ? appVersion : "Dev-Snapshot");

        // B. Spring Boot 版本
        versions.put("Spring Boot", SpringBootVersion.getVersion());

        // C. MyBatis Plus 版本 (通过核心类获取)
        versions.put("MyBatis Plus", getVersion(MybatisSqlSessionFactoryBuilder.class));

        // D. JDK 版本
        versions.put("JDK Version", System.getProperty("java.version"));

        // E. (可选) 如果引入了 Hutool 或 Redis，也可以加
         versions.put("Hutool", getVersion(cn.hutool.core.util.IdUtil.class));

        // 3. 格式化输出
        // 计算最大长度以便对齐
        int maxKeyLen = versions.keySet().stream().mapToInt(String::length).max().orElse(10);

        for (Map.Entry<String, String> entry : versions.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 拼接： :: Key (空格补齐) :: Value
            String paddedKey = String.format("%-" + maxKeyLen + "s", key);

            out.println(AnsiOutput.toString(
                    AnsiColor.BRIGHT_CYAN, " :: ",
                    AnsiColor.BRIGHT_YELLOW, paddedKey,
                    AnsiColor.BRIGHT_CYAN, " :: ",
                    AnsiColor.DEFAULT, value
            ));
        }
        out.println(); // 空一行
    }

    /**
     * 安全获取版本号的工具方法
     */
    private String getVersion(Class<?> clazz) {
        try {
            String version = clazz.getPackage().getImplementationVersion();
            return version != null ? version : "Unknown";
        } catch (Exception e) {
            return "N/A";
        }
    }
}