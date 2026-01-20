package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.cfg.ParamUpsertRequest;
import com.openx3.system.entity.cfg.CfgParam;
import com.openx3.system.service.CfgParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 参数管理接口（cfg_param）
 * 说明：配置类接口默认要求 admin 角色（由 Service 层统一校验）。
 */
@RestController
@RequestMapping("/api/cfg/params")
@RequiredArgsConstructor
public class CfgParamController {

    private final CfgParamService service;

    @GetMapping("/page")
    public R<Page<CfgParam>> page(@Validated PageRequest req,
                                 @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    @GetMapping("/{id}")
    public R<CfgParam> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping("/upsert")
    public R<String> upsert(@RequestBody @Validated ParamUpsertRequest req) {
        return R.success(service.upsert(req));
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

