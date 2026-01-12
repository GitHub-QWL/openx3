package com.openx3.system.repository;

import com.openx3.system.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单 Repository
 */
@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, String> {
    
    /**
     * 根据父ID查找子菜单
     */
    List<SysMenu> findByParentIdOrderBySortOrderAsc(String parentId);
    
    /**
     * 查找根菜单
     */
    List<SysMenu> findByParentIdIsNullOrderBySortOrderAsc();
}
