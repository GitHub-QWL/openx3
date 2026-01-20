package com.openx3.system.domain.dto.fieldpolicy;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FieldPolicyUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    /**
     * MASK/HIDDEN/ENCRYPT
     */
    @NotBlank(message = "policy不能为空")
    private String policy;

    private String policyParam;
}

