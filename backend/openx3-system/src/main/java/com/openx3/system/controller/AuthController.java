package com.openx3.system.controller;

import com.openx3.common.common.R;
import com.openx3.system.domain.dto.LoginRequest;
import com.openx3.system.domain.vo.LoginVO;
import com.openx3.system.domain.vo.UserVO;
import com.openx3.system.entity.SysUser;
import com.openx3.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody LoginRequest request) {
        SysUser user = authService.login(request.getUsername(), request.getPassword());
        
        LoginVO loginVO = new LoginVO();
        loginVO.setToken("mock-token-" + user.getId());  // TODO: 生成真实 Token
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        loginVO.setUser(userVO);
        
        return R.success("登录成功", loginVO);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public R<UserVO> getCurrentUser() {
        // TODO: 从 Token 中获取当前用户
        return R.error("未实现");
    }
}
