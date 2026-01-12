package com.openx3.system.entity;

import com.openx3.framework.jpa.entity.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_permission")
public class SysPermission extends JpaBaseEntity {
    
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;  // 权限码 (如: AUTH_SOH_SAVE)
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "object_code", length = 50)
    private String objectCode;  // 关联的业务对象
    
    @Column(name = "action", length = 50)
    private String action;  // 操作类型: SAVE, DELETE, QUERY, AUDIT
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
