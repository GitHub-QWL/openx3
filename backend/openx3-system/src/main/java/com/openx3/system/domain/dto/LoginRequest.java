package com.openx3.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO（账号层）
 */
@Data
public class LoginRequest {
    
    /**
     * 登录账号：username 或 mobile
     */
    @NotBlank(message = "账号不能为空")
    private String account;
    
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 可选：选择的员工ID（第二步 select 时必填）
     */
    private String employeeId;
}
