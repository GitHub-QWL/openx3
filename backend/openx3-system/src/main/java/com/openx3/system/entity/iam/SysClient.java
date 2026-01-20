package com.openx3.system.entity.iam;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 第三方客户端（机器账号凭证）
 * 对应表：sys_client
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_client")
public class SysClient extends MpBaseEntity {

    @TableField("client_id")
    private String clientId;

    @TableField("client_secret")
    private String clientSecret;

    private String appName;

    private String ipWhitelist;

    private Integer tokenValidity;

    @TableField("ref_employee_id")
    private String refEmployeeId;
}

