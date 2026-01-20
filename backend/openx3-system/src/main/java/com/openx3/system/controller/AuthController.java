package com.openx3.system.controller;

import com.openx3.common.common.R;
import com.openx3.system.domain.dto.LoginRequest;
import com.openx3.system.domain.dto.SwitchContextRequest;
import com.openx3.system.domain.vo.AuthInfoVO;
import com.openx3.system.domain.vo.AccountLoginVO;
import com.openx3.system.domain.vo.LoginVO;
import com.openx3.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、登出、获取当前用户信息
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 第一步：账号登录（返回可选员工列表，不签发 Token）
     */
    @PostMapping("/login")
    public R<AccountLoginVO> login(@RequestBody @Validated LoginRequest request) {
        return R.success(authService.login(request));
    }

    /**
     * 第二步：选择员工身份并签发 Token
     */
    @PostMapping("/select")
    public R<LoginVO> select(@RequestBody @Validated LoginRequest request) {
        return R.success(authService.select(request));
    }

    /**
     * 身份切换：在已登录状态下切换 employeeId，签发新 Token
     */
    @PostMapping("/switch")
    public R<LoginVO> switchContext(@RequestBody @Validated SwitchContextRequest request) {
        return R.success(authService.switchContext(request));
    }

    /**
     * 获取当前登录态信息（用户 + 角色 + 权限 + 菜单）
     */
    @GetMapping("/current")
    public R<AuthInfoVO> current() {
        return R.success(authService.getCurrentAuthInfo());
    }

    /**
     * 退出登录
     * @return 成功信息
     */
    @PostMapping("/logout")
    public R<String> logout() {
        authService.logout();
        return R.success("退出成功");
    }
}