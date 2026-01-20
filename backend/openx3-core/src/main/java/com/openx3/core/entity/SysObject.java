package com.openx3.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务对象定义
 * 对应表: sys_object
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_object")
public class SysObject extends MpBaseEntity {

    /** 业务对象编码 (如: SOH) */
    private String code;

    /** 业务对象名称 (如: 销售订单) */
    private String name;

    /** 对应的数据库表名 (如: t_sales_order_header) */
    private String tableName;
    
    private String stdScript; // 显式关联标准脚本
    
    private String speScript; // 显式关联特殊脚本

    /** 是否开启数据审计 (自动记录创建人/时间) */
    private Boolean isAudit;

    /** 类型 1=物理表, 2=混合 */
    private Integer storeType;
    /** 备注 */
    private String remark;
}