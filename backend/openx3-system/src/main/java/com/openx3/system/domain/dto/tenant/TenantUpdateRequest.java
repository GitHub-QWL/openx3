package com.openx3.system.domain.dto.tenant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    @NotBlank(message = "租户名称不能为空")
    private String name;
}

