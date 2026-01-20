package com.openx3.system.domain.dto.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    private String name;

    private String objectCode;

    private String action;

    private String description;
}

