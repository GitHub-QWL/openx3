package com.openx3.system.entity.relation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-部门关联（自定义数据权限）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_dept")
public class SysRoleDept extends MpBaseEntity {

    private String roleId;

    private String deptId;
}

