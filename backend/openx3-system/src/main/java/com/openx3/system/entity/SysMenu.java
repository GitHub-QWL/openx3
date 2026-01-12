package com.openx3.system.entity;

import com.openx3.framework.jpa.entity.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_menu")
public class SysMenu extends JpaBaseEntity {
    
    @Column(name = "parent_id", length = 32)
    private String parentId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "code", length = 50)
    private String code;
    
    @Column(name = "path", length = 255)
    private String path;
    
    @Column(name = "component", length = 255)
    private String component;
    
    @Column(name = "icon", length = 50)
    private String icon;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Column(name = "status")
    private Integer status;  // 0-禁用, 1-启用
}
