package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.cfg.DictCreateRequest;
import com.openx3.system.domain.dto.cfg.DictUpdateRequest;
import com.openx3.system.entity.cfg.CfgDict;
import com.openx3.system.service.CfgDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 字典管理接口（cfg_dict）
 * 说明：配置类接口默认要求 admin 角色（由 Service 层统一校验）。
 */
@RestController
@RequestMapping("/api/cfg/dicts")
@RequiredArgsConstructor
public class CfgDictController {

    private final CfgDictService service;

    @GetMapping("/page")
    public R<Page<CfgDict>> page(@Validated PageRequest req,
                                @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    @GetMapping("/{id}")
    public R<CfgDict> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated DictCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated DictUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

