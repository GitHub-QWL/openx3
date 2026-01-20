package com.openx3.system.domain.dto.fieldpolicy;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FieldPolicyCreateRequest {

    @NotBlank(message = "roleId不能为空")
    private String roleId;

    @NotBlank(message = "resourceCode不能为空")
    private String resourceCode;

    @NotBlank(message = "fieldName不能为空")
    private String fieldName;

    /**
     * MASK/HIDDEN/ENCRYPT
     */
    @NotBlank(message = "policy不能为空")
    private String policy;

    private String policyParam;
}

