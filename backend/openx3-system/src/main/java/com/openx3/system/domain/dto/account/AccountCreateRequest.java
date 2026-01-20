package com.openx3.system.domain.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建账号
 */
@Data
public class AccountCreateRequest {

    private String username;

    private String mobile;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 1=正常 0=禁用
     */
    private Integer status = 1;
}

