package com.openx3.system.domain.vo;

import lombok.Data;

/**
 * 用户 VO（视图对象）
 */
@Data
public class UserVO {
    
    private String id;
    private String username;
    private String nickname;
    private String email;
    private String mobile;
    private String avatar;
    private Integer status;
}
