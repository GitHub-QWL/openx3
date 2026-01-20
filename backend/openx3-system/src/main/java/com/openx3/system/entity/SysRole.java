package com.openx3.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends MpBaseEntity {

    @TableField("role_code")
    private String code;

    @TableField("role_name")
    private String name;

    @TableField("data_scope")
    private String dataScope;

    private String description;
}
