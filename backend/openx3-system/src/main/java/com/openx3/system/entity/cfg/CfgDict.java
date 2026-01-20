package com.openx3.system.entity.cfg;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典（配置层）
 * 对应表：cfg_dict
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfg_dict")
public class CfgDict extends MpBaseEntity {

    @TableField("dict_code")
    private String dictCode;

    @TableField("dict_name")
    private String dictName;

    /**
     * 1=启用 0=禁用
     */
    private Integer status;

    private String remark;
}

