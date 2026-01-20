package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.menu.MenuCreateRequest;
import com.openx3.system.domain.dto.menu.MenuUpdateRequest;
import com.openx3.system.entity.SysMenu;
import com.openx3.system.entity.relation.SysRoleMenu;
import com.openx3.system.mapper.SysMenuMapper;
import com.openx3.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 菜单管理服务（sys_menu）
 */
@Service
@RequiredArgsConstructor
public class IamMenuService extends IamAdminService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public Page<SysMenu> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<SysMenu> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<SysMenu> qw = new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysMenu::getTitle, keyword).or().like(SysMenu::getPerms, keyword).or().like(SysMenu::getPath, keyword));
        }
        qw.orderByAsc(SysMenu::getSortNo);
        return menuMapper.selectPage(page, qw);
    }

    public SysMenu get(String id) {
        checkAdmin();
        SysMenu m = menuMapper.selectById(id);
        if (m == null || m.getDelFlag() != null && m.getDelFlag() != 0) {
            throw new BusinessException(404, "菜单不存在");
        }
        return m;
    }

    public String create(MenuCreateRequest req) {
        checkAdmin();
        SysMenu m = new SysMenu();
        m.setParentId(StringUtils.hasText(req.getParentId()) ? req.getParentId() : "0");
        m.setTitle(req.getTitle());
        m.setPath(req.getPath());
        m.setComponent(req.getComponent());
        m.setPerms(req.getPerms());
        m.setIcon(req.getIcon());
        m.setType(req.getType());
        m.setSortNo(req.getSortNo());
        m.setVisible(req.getVisible());
        menuMapper.insert(m);
        return m.getId();
    }

    public void update(MenuUpdateRequest req) {
        checkAdmin();
        SysMenu m = menuMapper.selectById(req.getId());
        if (m == null || m.getDelFlag() != null && m.getDelFlag() != 0) {
            throw new BusinessException(404, "菜单不存在");
        }
        m.setParentId(StringUtils.hasText(req.getParentId()) ? req.getParentId() : "0");
        m.setTitle(req.getTitle());
        m.setPath(req.getPath());
        m.setComponent(req.getComponent());
        m.setPerms(req.getPerms());
        m.setIcon(req.getIcon());
        m.setType(req.getType());
        m.setSortNo(req.getSortNo());
        m.setVisible(req.getVisible());
        menuMapper.updateById(m);
    }

    public void delete(String id) {
        checkAdmin();

        long child = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getDelFlag, 0));
        if (child > 0) {
            throw new BusinessException(400, "存在子菜单，禁止删除");
        }

        long used = roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getMenuId, id)
                .eq(SysRoleMenu::getDelFlag, 0));
        if (used > 0) {
            throw new BusinessException(400, "菜单已被角色引用，禁止删除");
        }

        menuMapper.deleteById(id);
    }
}

