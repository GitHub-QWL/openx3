package com.openx3.framework.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MyBatis Plus 专用基类
 * 依赖：compileOnly mybatis-plus-annotation
 *
 * 按《OpenX3 开发与工程规范手册》要求：
 * - 所有 sys_ / dat_ / cfg_ 表统一包含审计字段 + tenant_id
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

    /**
     * 租户ID（SaaS 预留）
     * 说明：即使采用 schema-per-tenant，也保留 tenant_id 作为跨租户审计/迁移的兜底字段
     */
    @TableField(fill = FieldFill.INSERT)
    private String tenantId;
}