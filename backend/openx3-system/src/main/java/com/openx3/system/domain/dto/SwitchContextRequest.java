package com.openx3.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 切换业务身份请求
 */
@Data
public class SwitchContextRequest {

    @NotBlank(message = "employeeId不能为空")
    private String employeeId;
}

