package com.openx3.system.domain.vo;

import lombok.Data;

/**
 * 创建第三方客户端返回（只在创建/轮换时返回一次明文 secret）
 */
@Data
public class ClientCreateVO {

    private String id;
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String appName;
    private String refEmployeeId;
}

