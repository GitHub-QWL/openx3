package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.cfg.DictItemCreateRequest;
import com.openx3.system.domain.dto.cfg.DictItemUpdateRequest;
import com.openx3.system.entity.cfg.CfgDictItem;
import com.openx3.system.service.CfgDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 字典项管理接口（cfg_dict_item）
 * 说明：配置类接口默认要求 admin 角色（由 Service 层统一校验）。
 */
@RestController
@RequestMapping("/api/cfg/dict-items")
@RequiredArgsConstructor
public class CfgDictItemController {

    private final CfgDictItemService service;

    @GetMapping("/page")
    public R<Page<CfgDictItem>> page(@Validated PageRequest req,
                                    @RequestParam String dictId) {
        return R.success(service.page(req, dictId));
    }

    @GetMapping("/{id}")
    public R<CfgDictItem> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated DictItemCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated DictItemUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

