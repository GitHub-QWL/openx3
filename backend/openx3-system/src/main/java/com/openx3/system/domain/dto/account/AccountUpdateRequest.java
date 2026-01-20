package com.openx3.system.domain.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新账号基础信息（不含密码）
 */
@Data
public class AccountUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    private String username;

    private String mobile;

    /**
     * 1=正常 0=禁用
     */
    private Integer status;
}

