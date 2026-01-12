package com.openx3.system.entity;

import com.openx3.framework.jpa.entity.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user")
public class SysUser extends JpaBaseEntity {
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "real_name", length = 100)
    private String realName;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "avatar", length = 255)
    private String avatar;
    
    @Column(name = "status")
    private Integer status;  // 0-禁用, 1-启用
    
    @Column(name = "dept_id", length = 32)
    private String deptId;
}
