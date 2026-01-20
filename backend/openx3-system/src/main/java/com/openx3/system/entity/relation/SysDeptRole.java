package com.openx3.system.entity.relation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门-角色关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept_role")
public class SysDeptRole extends MpBaseEntity {

    private String deptId;

    private String roleId;
}

