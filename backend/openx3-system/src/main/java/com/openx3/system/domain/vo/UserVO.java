package com.openx3.system.domain.vo;

import lombok.Data;

/**
 * 用户 VO（视图对象）
 */
@Data
public class UserVO {
    
    private String id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
}
