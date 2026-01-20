package com.openx3.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends MpBaseEntity {

    private String code;  // 权限码 (如: AUTH_SOH_SAVE)

    private String name;

    private String objectCode;  // 关联的业务对象

    private String action;  // 操作类型: SAVE, DELETE, QUERY, AUDIT

    private String description;
}
