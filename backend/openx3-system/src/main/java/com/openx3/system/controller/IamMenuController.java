package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.menu.MenuCreateRequest;
import com.openx3.system.domain.dto.menu.MenuUpdateRequest;
import com.openx3.system.entity.SysMenu;
import com.openx3.system.service.IamMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 菜单管理接口（sys_menu）
 * 说明：菜单 perms 用于功能权限控制（前端按钮/菜单显示 + 后端权限判断）。
 *
 * 提供对系统菜单的增删改查 REST API 接口，支持分页查询、获取详情、创建、更新和删除菜单等功能。
 *
 * @author author_name
 * @date 2026-01-20
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/iam/menus")
@RequiredArgsConstructor
public class IamMenuController {

    private final IamMenuService service;

    /**
     * 分页查询菜单列表
     *
     * 根据分页参数和关键词查询菜单列表，支持按关键词对菜单名称等字段进行模糊搜索
     *
     * @param req 分页请求参数，包含页码和每页大小
     * @param keyword 搜索关键词，可选参数，用于模糊匹配菜单相关信息
     * @return 成功响应，包含分页的菜单信息
     */
    @GetMapping("/page")
    public R<Page<SysMenu>> page(@Validated PageRequest req,
                                @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    /**
     * 获取指定ID的菜单详情
     *
     * 根据菜单唯一标识符获取对应的菜单详细信息
     *
     * @param id 菜单唯一标识符
     * @return 成功响应，包含指定ID的菜单信息
     */
    @GetMapping("/{id}")
    public R<SysMenu> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    /**
     * 创建新菜单
     *
     * 根据请求参数创建一个新的菜单记录
     *
     * @param req 菜单创建请求对象，包含创建所需的所有参数
     * @return 成功响应，包含新创建菜单的唯一标识符
     */
    @PostMapping
    public R<String> create(@RequestBody @Validated MenuCreateRequest req) {
        return R.success(service.create(req));
    }

    /**
     * 更新菜单信息
     *
     * 根据请求参数更新指定菜单的信息
     *
     * @param req 菜单更新请求对象，包含需要更新的参数
     * @return 成功响应，提示更新成功信息
     */
    @PutMapping
    public R<String> update(@RequestBody @Validated MenuUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    /**
     * 删除指定菜单
     *
     * 根据菜单ID执行删除操作，通常为软删除
     *
     * @param id 需要删除的菜单唯一标识符
     * @return 成功响应，提示删除成功信息
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

