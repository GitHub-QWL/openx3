package com.openx3.system.domain.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新第三方客户端（不含 secret）
 */
@Data
public class ClientUpdateRequest {

    @NotBlank(message = "id不能为空")
    private String id;

    private String appName;

    private String ipWhitelist;

    private Integer tokenValidity;

    /**
     * 关联的虚拟员工（一般不建议修改；提供给特殊迁移场景）
     */
    private String refEmployeeId;
}

