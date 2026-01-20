package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.cfg.LocalMenuCreateRequest;
import com.openx3.system.domain.dto.cfg.LocalMenuUpdateRequest;
import com.openx3.system.entity.cfg.CfgLocalMenu;
import com.openx3.system.service.CfgLocalMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 本地菜单管理接口（cfg_local_menu）
 * 说明：用于对接/兼容外部系统菜单（参考 Sage X3 的本地功能菜单）。
 */
@RestController
@RequestMapping("/api/cfg/local-menus")
@RequiredArgsConstructor
public class CfgLocalMenuController {

    private final CfgLocalMenuService service;

    @GetMapping("/page")
    public R<Page<CfgLocalMenu>> page(@Validated PageRequest req,
                                     @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    @GetMapping("/{id}")
    public R<CfgLocalMenu> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated LocalMenuCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated LocalMenuUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

