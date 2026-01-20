package com.openx3.system.domain.dto.dept;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeptCreateRequest {

    @NotBlank(message = "tenantId不能为空")
    private String tenantId;

    /**
     * 父部门ID，默认 0
     */
    private String parentId = "0";

    @NotBlank(message = "deptName不能为空")
    private String deptName;

    private Integer sortNo = 0;

    /**
     * 1=正常 0=禁用
     */
    private Integer status = 1;
}

