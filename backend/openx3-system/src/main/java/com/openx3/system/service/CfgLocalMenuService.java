package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.cfg.LocalMenuCreateRequest;
import com.openx3.system.domain.dto.cfg.LocalMenuUpdateRequest;
import com.openx3.system.entity.cfg.CfgLocalMenu;
import com.openx3.system.mapper.CfgLocalMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 本地菜单服务（cfg_local_menu）
 * 说明：用于维护对接/跳转类菜单（参考 Sage X3 的功能菜单思路）。
 */
@Service
@RequiredArgsConstructor
public class CfgLocalMenuService extends IamAdminService {

    private final CfgLocalMenuMapper menuMapper;

    public Page<CfgLocalMenu> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<CfgLocalMenu> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<CfgLocalMenu> qw = new LambdaQueryWrapper<CfgLocalMenu>()
                .eq(CfgLocalMenu::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.like(CfgLocalMenu::getTitle, keyword);
        }
        qw.orderByAsc(CfgLocalMenu::getSortNo);
        return menuMapper.selectPage(page, qw);
    }

    public CfgLocalMenu get(String id) {
        checkAdmin();
        CfgLocalMenu m = menuMapper.selectById(id);
        if (m == null || m.getDelFlag() != null && m.getDelFlag() != 0) {
            throw new BusinessException(404, "本地菜单不存在");
        }
        return m;
    }

    public String create(LocalMenuCreateRequest req) {
        checkAdmin();
        CfgLocalMenu m = new CfgLocalMenu();
        m.setParentId(StringUtils.hasText(req.getParentId()) ? req.getParentId() : "0");
        m.setTitle(req.getTitle());
        m.setFunctionCode(req.getFunctionCode());
        m.setUrl(req.getUrl());
        m.setIcon(req.getIcon());
        m.setSortNo(req.getSortNo());
        m.setStatus(req.getStatus());
        menuMapper.insert(m);
        return m.getId();
    }

    public void update(LocalMenuUpdateRequest req) {
        checkAdmin();
        CfgLocalMenu m = menuMapper.selectById(req.getId());
        if (m == null || m.getDelFlag() != null && m.getDelFlag() != 0) {
            throw new BusinessException(404, "本地菜单不存在");
        }
        m.setParentId(StringUtils.hasText(req.getParentId()) ? req.getParentId() : "0");
        m.setTitle(req.getTitle());
        m.setFunctionCode(req.getFunctionCode());
        m.setUrl(req.getUrl());
        m.setIcon(req.getIcon());
        m.setSortNo(req.getSortNo());
        m.setStatus(req.getStatus());
        menuMapper.updateById(m);
    }

    public void delete(String id) {
        checkAdmin();
        long child = menuMapper.selectCount(new LambdaQueryWrapper<CfgLocalMenu>()
                .eq(CfgLocalMenu::getParentId, id)
                .eq(CfgLocalMenu::getDelFlag, 0));
        if (child > 0) throw new BusinessException(400, "存在子菜单，禁止删除");
        menuMapper.deleteById(id);
    }
}

