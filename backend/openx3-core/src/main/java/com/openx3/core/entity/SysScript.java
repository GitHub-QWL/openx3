package com.openx3.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务脚本实体
 * 对应数据库表: sys_script
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_script")
public class SysScript extends MpBaseEntity {

    /**
     * 脚本编码 (唯一键, 如: SPE_SOH_SAVE)
     */
    private String code;

    /**
     * 脚本名称 (如: 销售订单保存前逻辑)
     */
    private String name;

    /**
     * 备注
     */
    private String remark;


    /**
     * Groovy 源代码
     */
    private String content;

    /**
     * 是否启用 (未启用的脚本即使发布也不会被加载)
     */
    private Integer activeFlag;

    

    /**
     * 编译后的 Class 对象缓存 (不序列化到数据库)
     */
    private transient Class<?> compiledClass;
}