package com.openx3.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段权限策略
 * 对应表：sys_field_policy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_field_policy")
public class SysFieldPolicy extends MpBaseEntity {

    private String roleId;

    private String resourceCode;

    private String fieldName;

    private String policy;

    @TableField("policy_param")
    private String policyParam;
}

