package com.openx3.framework.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MyBatis Plus 专用基类
 * 依赖：compileOnly mybatis-plus-annotation
 */
@Data
public abstract class MpBaseEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID) // 雪花算法 ID
    private String id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 配合 MetaObjectHandler
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableLogic // 逻辑删除
    private Integer delFlag;

    @Version // 乐观锁
    private Integer version;
}