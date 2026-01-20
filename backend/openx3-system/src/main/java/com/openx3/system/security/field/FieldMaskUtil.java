package com.openx3.system.security.field;

/**
 * 字段脱敏工具（用于 MASK 策略）
 */
public class FieldMaskUtil {

    private FieldMaskUtil() {
    }

    /**
     * 通用脱敏：保留前 3 后 4，其余用 *
     */
    public static String mask(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.length() <= 7) return "******";
        int prefix = 3;
        int suffix = 4;
        StringBuilder sb = new StringBuilder();
        sb.append(v, 0, prefix);
        sb.append("*".repeat(Math.max(0, v.length() - prefix - suffix)));
        sb.append(v.substring(v.length() - suffix));
        return sb.toString();
    }
}

