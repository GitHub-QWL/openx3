package com.openx3.system.repository;

import com.openx3.system.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限 Repository
 */
@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, String> {
    
    /**
     * 根据编码查找权限
     */
    Optional<SysPermission> findByCode(String code);
    
    /**
     * 根据对象编码查找权限
     */
    List<SysPermission> findByObjectCode(String objectCode);
}
