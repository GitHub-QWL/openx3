package com.openx3.system.entity.cfg;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 本地菜单（配置层，用于对接/兼容外部系统菜单）
 * 对应表：cfg_local_menu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfg_local_menu")
public class CfgLocalMenu extends MpBaseEntity {

    private String parentId;

    private String title;

    @TableField("function_code")
    private String functionCode;

    private String url;

    private String icon;

    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 1=启用 0=禁用
     */
    private Integer status;
}

