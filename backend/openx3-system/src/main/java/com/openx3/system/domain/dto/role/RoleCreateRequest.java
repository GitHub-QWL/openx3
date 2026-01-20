package com.openx3.system.domain.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleCreateRequest {

    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "name不能为空")
    private String name;

    /**
     * ALL/DEPT_AND_CHILD/DEPT/SELF/CUSTOM
     */
    private String dataScope = "SELF";

    private String description;
}

