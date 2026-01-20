package com.openx3.system.entity.iam;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openx3.framework.mybatis.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统账号实体类（认证层）
 * 用于用户身份认证的账号信息管理
 * 对应数据库表：sys_account
 *
 * @author author_name
 * @date 2026-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_account")
public class SysAccount extends MpBaseEntity {

    /**
     * 用户名
     * 唯一标识用户的登录名称，用于系统登录验证
     */
    private String username;

    /**
     * 手机号
     * 用户绑定的手机号码，可用于登录或找回密码等操作
     */
    private String mobile;

    /**
     * 密码
     * 经过加密处理后的用户密码，使用安全加密算法存储
     */
    private String password;

    /**
     * 加密盐值
     * 用于增强密码安全性，防止彩虹表攻击的随机盐值
     */
    private String salt;

    /**
     * 状态：1=正常 0=禁用
     * 标识账号的启用状态，禁用状态的账号无法进行登录操作
     */
    private Integer status;
}

