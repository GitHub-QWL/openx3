package com.openx3.system.entity.relation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工-岗位关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_employee_post")
public class SysEmployeePost extends MpBaseEntity {

    private String employeeId;

    private String postId;
}

