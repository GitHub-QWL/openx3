package com.openx3.framework.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA 专用基类
 * 依赖：compileOnly jakarta.persistence-api
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // 启用审计
public abstract class JpaBaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增 ID (或改为 UUID)
    private Long id;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    // JPA 审计需要 Spring Security 上下文配合 AuditorAware
    // 如果还没配 AuditorAware，这两个字段可能为空，或者手动 PrePersist 处理
    // @CreatedBy
    // private String createBy;
    // @LastModifiedBy
    // private String updateBy;

    // 逻辑删除通常 JPA 手动处理，或使用 Hibernate @SQLDelete
    private Integer delFlag = 0;

    @Version
    private Integer version;
}