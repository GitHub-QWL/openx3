package com.openx3.system.entity.iam;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门（组织）
 * 对应表：sys_dept
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends MpBaseEntity {

    private String parentId;

    @TableField("dept_name")
    private String deptName;

    private String treePath;

    private Integer sortNo;

    /**
     * 1=正常 0=禁用
     */
    private Integer status;
}

