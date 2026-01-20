package com.openx3.system.domain.dto.tenant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantCreateRequest {

    @NotBlank(message = "租户名称不能为空")
    private String name;
}

