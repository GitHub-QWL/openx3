package com.openx3.system.entity.cfg;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典项（配置层）
 * 对应表：cfg_dict_item
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfg_dict_item")
public class CfgDictItem extends MpBaseEntity {

    private String dictId;

    @TableField("item_value")
    private String itemValue;

    @TableField("item_label")
    private String itemLabel;

    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 1=启用 0=禁用
     */
    private Integer status;
}

