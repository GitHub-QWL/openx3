package com.openx3.system.entity.iam;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位
 * 对应表：sys_post
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class SysPost extends MpBaseEntity {

    @TableField("post_code")
    private String postCode;

    @TableField("post_name")
    private String postName;

    /**
     * 1=正常 0=禁用
     */
    private Integer status;
}

