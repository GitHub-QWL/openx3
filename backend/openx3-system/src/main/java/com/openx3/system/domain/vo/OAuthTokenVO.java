package com.openx3.system.domain.vo;

import lombok.Data;

/**
 * OAuth2 Token 响应（Client Credentials）
 */
@Data
public class OAuthTokenVO {

    private String accessToken;
    private String tokenType = "Bearer";
    private Integer expiresIn;
}

