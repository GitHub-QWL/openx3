package com.openx3.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * ID 生成器工具类
 * 使用雪花算法生成分布式唯一ID
 */
public class IdGenerator {
    
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);
    
    /**
     * 生成唯一ID（字符串格式）
     */
    public static String nextId() {
        return String.valueOf(SNOWFLAKE.nextId());
    }
    
    /**
     * 生成唯一ID（Long格式）
     */
    public static Long nextLongId() {
        return SNOWFLAKE.nextId();
    }
}
