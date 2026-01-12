package com.openx3.system.domain.vo;

import lombok.Data;

/**
 * 登录响应 VO
 */
@Data
public class LoginVO {
    
    private String token;
    private UserVO user;
}
