package com.openx3.system.entity.relation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位-角色关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post_role")
public class SysPostRole extends MpBaseEntity {

    private String postId;

    private String roleId;
}

