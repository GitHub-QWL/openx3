package com.openx3.system.domain.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    @NotBlank(message = "name不能为空")
    private String name;

    /**
     * ALL/DEPT_AND_CHILD/DEPT/SELF/CUSTOM
     */
    private String dataScope = "SELF";

    private String description;
}

