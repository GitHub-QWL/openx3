package com.openx3.system.domain.vo;

import lombok.Data;

/**
 * 账号 VO（认证层）
 */
@Data
public class AccountVO {

    private String id;
    private String username;
    private String mobile;
    private Integer status;
}

