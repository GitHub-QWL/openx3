package com.openx3.system.domain.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重置账号密码
 */
@Data
public class AccountResetPasswordRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}

