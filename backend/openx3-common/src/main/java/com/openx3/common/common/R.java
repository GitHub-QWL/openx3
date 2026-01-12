package com.openx3.common.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 所有 HTTP 接口必须返回此对象
 */
@Data
public class R<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 业务状态码 (非 HTTP 状态码)
     * 200: 成功
     * 其他: 业务错误码
     */
    private Integer code;
    
    /**
     * 是否成功 (前端通过此字段判断)
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 实际载荷数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public R() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public R(Integer code, Boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应
     */
    public static <T> R<T> success() {
        return new R<>(200, true, "操作成功", null);
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> success(T data) {
        return new R<>(200, true, "操作成功", data);
    }
    
    /**
     * 成功响应（带消息和数据）
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(200, true, message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> R<T> error(String message) {
        return new R<>(500, false, message, null);
    }
    
    /**
     * 失败响应（带状态码）
     */
    public static <T> R<T> error(Integer code, String message) {
        return new R<>(code, false, message, null);
    }
}
