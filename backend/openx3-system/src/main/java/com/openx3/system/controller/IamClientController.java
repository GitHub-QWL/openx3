package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.client.ClientCreateRequest;
import com.openx3.system.domain.dto.client.ClientRotateSecretRequest;
import com.openx3.system.domain.dto.client.ClientUpdateRequest;
import com.openx3.system.domain.vo.ClientCreateVO;
import com.openx3.system.entity.iam.SysClient;
import com.openx3.system.service.IamClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 第三方系统对接客户端管理接口（sys_client）
 */
@RestController
@RequestMapping("/api/iam/clients")
@RequiredArgsConstructor
public class IamClientController {

    private final IamClientService service;

    @GetMapping("/page")
    public R<Page<SysClient>> page(@Validated PageRequest req,
                                  @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    @GetMapping("/{id}")
    public R<SysClient> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<ClientCreateVO> create(@RequestBody @Validated ClientCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated ClientUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @PostMapping("/rotate-secret")
    public R<ClientCreateVO> rotateSecret(@RequestBody @Validated ClientRotateSecretRequest req) {
        return R.success(service.rotateSecret(req));
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

