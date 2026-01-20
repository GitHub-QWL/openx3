package com.openx3.system.domain.dto.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionCreateRequest {

    @NotBlank(message = "code不能为空")
    private String code;

    private String name;

    private String objectCode;

    private String action;

    private String description;
}

