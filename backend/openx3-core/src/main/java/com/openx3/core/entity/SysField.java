package com.openx3.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段定义
 * 对应表: sys_field
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_field")
public class SysField extends MpBaseEntity {

    /** 关联的对象编码 */
    private String objectCode;

    /** 字段名 (如: order_no) */
    private String fieldName;

    /** 字段标题 (如: 订单号) */
    private String fieldLabel;

    /** 数据类型 (VARCHAR, INT, DATETIME, JSON) */
    private String fieldType;

    /** 控件类型 (Text, Select, DatePicker) */
    private String widgetType;

    /** 控件SON 配置 */
    private String widgetConfig;

    /** 是否必填 */
    private Boolean isRequired;

    /** 排序号 */
    private Integer sortNo;
}