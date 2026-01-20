package com.openx3.system.entity.iam;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工（业务身份）
 * 对应表：sys_employee
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_employee")
public class SysEmployee extends MpBaseEntity {

    private String accountId;

    private String deptId;

    @TableField("emp_no")
    private String empNo;

    @TableField("real_name")
    private String realName;

    private Boolean isMain;

    /**
     * 是否自然人（机器账号/虚拟员工 = false）
     */
    @TableField("is_human")
    private Boolean isHuman;
}

