package com.openx3.system.domain.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建第三方客户端（sys_client）
 */
@Data
public class ClientCreateRequest {

    @NotBlank(message = "tenantId不能为空")
    private String tenantId;

    @NotBlank(message = "clientId不能为空")
    private String clientId;

    /**
     * 可选：不传则后端生成，并在创建响应中返回一次明文
     */
    private String clientSecret;

    private String appName;

    /**
     * 逗号分隔，可包含 CIDR（IPv4）
     */
    private String ipWhitelist;

    /**
     * Token 有效期（秒），默认 7200
     */
    private Integer tokenValidity = 7200;

    /**
     * 虚拟员工归属部门（不传默认 dept-root）
     */
    private String deptId;
}

