package com.openx3.system.service;

import com.openx3.common.constants.Constants;
import com.openx3.common.exception.BusinessException;
import com.openx3.common.utils.PasswordUtil;
import com.openx3.system.entity.SysUser;
import com.openx3.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final SysUserRepository userRepository;


    /**
     * 用户登录
     */
    public SysUser login(String username, String password) {
        Optional<SysUser> userOpt = userRepository.findByUsernameAndDelFlag(username, Constants.DEL_FLAG_NORMAL);
        
        if (userOpt.isEmpty()) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        SysUser user = userOpt.get();
        
        // 密码验证
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(403, "用户已被禁用");
        }
        
        return user;
    }
    
    /**
     * 根据用户名查找用户
     */
    public Optional<SysUser> findByUsername(String username) {
        return userRepository.findByUsernameAndDelFlag(username, Constants.DEL_FLAG_NORMAL);
    }
}
