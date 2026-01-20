package com.openx3.system.entity.cfg;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数表（配置层）
 * 对应表：cfg_param
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfg_param")
public class CfgParam extends MpBaseEntity {

    @TableField("param_code")
    private String paramCode;

    @TableField("param_name")
    private String paramName;

    @TableField("param_value")
    private String paramValue;

    private String remark;
}

