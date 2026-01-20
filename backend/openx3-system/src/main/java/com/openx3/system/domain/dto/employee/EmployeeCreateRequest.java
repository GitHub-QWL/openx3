package com.openx3.system.domain.dto.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeCreateRequest {

    @NotBlank(message = "accountId不能为空")
    private String accountId;

    @NotBlank(message = "tenantId不能为空")
    private String tenantId;

    private String deptId;

    private String empNo;

    @NotBlank(message = "realName不能为空")
    private String realName;

    private Boolean isMain = true;
}

