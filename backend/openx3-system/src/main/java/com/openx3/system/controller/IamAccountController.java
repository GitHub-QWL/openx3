package com.openx3.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.common.R;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.account.AccountCreateRequest;
import com.openx3.system.domain.dto.account.AccountResetPasswordRequest;
import com.openx3.system.domain.dto.account.AccountUpdateRequest;
import com.openx3.system.domain.vo.AccountVO;
import com.openx3.system.service.IamAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 账号管理接口（sys_account）
 * 说明：管理端接口默认要求 admin 角色（由 Service 层统一校验）。
 */
@RestController
@RequestMapping("/api/iam/accounts")
@RequiredArgsConstructor
public class IamAccountController {

    private final IamAccountService service;

    @GetMapping("/page")
    public R<Page<AccountVO>> page(@Validated PageRequest req,
                                  @RequestParam(required = false) String keyword) {
        return R.success(service.page(req, keyword));
    }

    @GetMapping("/{id}")
    public R<AccountVO> get(@PathVariable String id) {
        return R.success(service.get(id));
    }

    @PostMapping
    public R<String> create(@RequestBody @Validated AccountCreateRequest req) {
        return R.success(service.create(req));
    }

    @PutMapping
    public R<String> update(@RequestBody @Validated AccountUpdateRequest req) {
        service.update(req);
        return R.success("更新成功");
    }

    @PostMapping("/reset-password")
    public R<String> resetPassword(@RequestBody @Validated AccountResetPasswordRequest req) {
        service.resetPassword(req);
        return R.success("重置成功");
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        service.delete(id);
        return R.success("删除成功");
    }
}

