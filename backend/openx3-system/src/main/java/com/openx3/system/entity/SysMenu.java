package com.openx3.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends MpBaseEntity {

    private String parentId;

    /**
     * 菜单名称（DB 字段：title）
     */
    private String title;

    private String path;

    private String component;

    /**
     * 权限标识（如：sys:user:list）
     */
    private String perms;

    private String icon;

    /**
     * 类型 (0=目录, 1=菜单, 2=按钮)
     */
    private Integer type;

    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 是否显示
     */
    private Boolean visible;
}
