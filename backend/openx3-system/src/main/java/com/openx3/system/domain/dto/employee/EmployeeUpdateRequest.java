package com.openx3.system.domain.dto.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    private String deptId;

    private String empNo;

    @NotBlank(message = "realName不能为空")
    private String realName;

    private Boolean isMain = true;
}

