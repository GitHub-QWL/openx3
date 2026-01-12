package com.openx3.system.repository;

import com.openx3.system.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色 Repository
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, String> {
    
    /**
     * 根据编码查找角色
     */
    Optional<SysRole> findByCode(String code);
}
