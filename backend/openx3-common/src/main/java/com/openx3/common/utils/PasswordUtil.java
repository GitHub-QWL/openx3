package com.openx3.common.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具类
 * 使用 BCrypt 进行密码加密和验证
 */
public class PasswordUtil {
    
    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword);
    }
    
    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
