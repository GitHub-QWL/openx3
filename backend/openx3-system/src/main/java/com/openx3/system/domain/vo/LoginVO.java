package com.openx3.system.domain.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录成功响应数据封装
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 认证 Token (前端需将其放入 Header: Authorization: Bearer {token})
     */
    private String token;

    /**
     * 账号 ID（sub）
     */
    private String accountId;

    /**
     * 当前员工 ID（uid）
     */
    private String employeeId;

    /**
     * 展示名（优先员工 realName，其次账号 username）
     */
    private String displayName;

    // 如果后续有需求，可以在这里扩展 avatar(头像), roleNames(角色名列表) 等字段
}