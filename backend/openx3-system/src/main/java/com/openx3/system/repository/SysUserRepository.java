package com.openx3.system.repository;

import com.openx3.system.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户 Repository
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, String> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<SysUser> findByUsernameAndDelFlag(String username, Integer delFlag);
}
