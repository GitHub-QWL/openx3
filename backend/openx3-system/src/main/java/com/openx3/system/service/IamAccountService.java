package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openx3.common.exception.BusinessException;
import com.openx3.common.utils.PasswordUtil;
import com.openx3.system.domain.dto.PageRequest;
import com.openx3.system.domain.dto.account.AccountCreateRequest;
import com.openx3.system.domain.dto.account.AccountResetPasswordRequest;
import com.openx3.system.domain.dto.account.AccountUpdateRequest;
import com.openx3.system.domain.vo.AccountVO;
import com.openx3.system.entity.iam.SysAccount;
import com.openx3.system.mapper.SysAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 账号 CRUD 服务（认证层）
 *
 * 提供对系统账号的增删改查操作，包括分页查询、创建、更新、删除和密码重置等功能。
 * 此服务继承自 IamAdminService，具备管理员权限检查功能。
 *
 * @author openx3
 * @date 2025-05-13
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class IamAccountService extends IamAdminService {

    private final SysAccountMapper accountMapper;

    /**
     * 分页查询账号信息
     *
     * 根据关键词对用户名和手机号进行模糊查询，并返回分页结果
     *
     * @param req 查询分页参数，包含页码和每页大小
     * @param keyword 搜索关键词，用于匹配用户名或手机号
     * @return 包含账号信息的分页对象
     * @throws BusinessException 当管理员权限验证失败时抛出异常
     */
    public Page<AccountVO> page(PageRequest req, String keyword) {
        checkAdmin();
        Page<SysAccount> page = new Page<>(req.getPageNo(), req.getPageSize());

        LambdaQueryWrapper<SysAccount> qw = new LambdaQueryWrapper<SysAccount>()
                .eq(SysAccount::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysAccount::getUsername, keyword).or().like(SysAccount::getMobile, keyword));
        }
        Page<SysAccount> p = accountMapper.selectPage(page, qw);

        Page<AccountVO> out = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        out.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return out;
    }

    /**
     * 获取指定ID的账号详情
     *
     * 根据账号ID查询并返回对应的账号信息
     *
     * @param id 账号唯一标识符
     * @return 账号视图对象，包含账号的基本信息
     * @throws BusinessException 当账号不存在或管理员权限验证失败时抛出异常
     */
    public AccountVO get(String id) {
        checkAdmin();
        SysAccount a = accountMapper.selectById(id);
        if (a == null || a.getDelFlag() != null && a.getDelFlag() != 0) {
            throw new BusinessException(404, "账号不存在");
        }
        return toVO(a);
    }

    /**
     * 创建新账号
     *
     * 根据请求参数创建一个新的账号记录，包含用户名、手机号、密码和状态等信息
     *
     * @param req 账号创建请求对象，包含创建所需的所有参数
     * @return 新创建账号的唯一标识符
     * @throws BusinessException 当缺少必要参数、用户名或手机号重复或管理员权限验证失败时抛出异常
     */
    public String create(AccountCreateRequest req) {
        checkAdmin();
        if (!StringUtils.hasText(req.getUsername()) && !StringUtils.hasText(req.getMobile())) {
            throw new BusinessException(400, "username/mobile 至少提供一个");
        }

        // 唯一性校验（软删除数据不计入）
        if (StringUtils.hasText(req.getUsername())) {
            long cnt = accountMapper.selectCount(new LambdaQueryWrapper<SysAccount>()
                    .eq(SysAccount::getUsername, req.getUsername())
                    .eq(SysAccount::getDelFlag, 0));
            if (cnt > 0) throw new BusinessException(400, "username已存在");
        }
        if (StringUtils.hasText(req.getMobile())) {
            long cnt = accountMapper.selectCount(new LambdaQueryWrapper<SysAccount>()
                    .eq(SysAccount::getMobile, req.getMobile())
                    .eq(SysAccount::getDelFlag, 0));
            if (cnt > 0) throw new BusinessException(400, "mobile已存在");
        }

        SysAccount a = new SysAccount();
        a.setUsername(req.getUsername());
        a.setMobile(req.getMobile());
        a.setPassword(PasswordUtil.encode(req.getPassword()));
        a.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        accountMapper.insert(a);
        return a.getId();
    }

    /**
     * 更新账号信息
     *
     * 根据请求参数更新指定账号的信息，支持更新用户名、手机号和状态等字段
     *
     * @param req 账号更新请求对象，包含需要更新的参数
     * @throws BusinessException 当账号不存在、用户名或手机号重复或管理员权限验证失败时抛出异常
     */
    public void update(AccountUpdateRequest req) {
        checkAdmin();
        SysAccount a = accountMapper.selectById(req.getId());
        if (a == null || a.getDelFlag() != null && a.getDelFlag() != 0) {
            throw new BusinessException(404, "账号不存在");
        }

        if (StringUtils.hasText(req.getUsername()) && !req.getUsername().equals(a.getUsername())) {
            long cnt = accountMapper.selectCount(new LambdaQueryWrapper<SysAccount>()
                    .eq(SysAccount::getUsername, req.getUsername())
                    .eq(SysAccount::getDelFlag, 0));
            if (cnt > 0) throw new BusinessException(400, "username已存在");
            a.setUsername(req.getUsername());
        }
        if (StringUtils.hasText(req.getMobile()) && !req.getMobile().equals(a.getMobile())) {
            long cnt = accountMapper.selectCount(new LambdaQueryWrapper<SysAccount>()
                    .eq(SysAccount::getMobile, req.getMobile())
                    .eq(SysAccount::getDelFlag, 0));
            if (cnt > 0) throw new BusinessException(400, "mobile已存在");
            a.setMobile(req.getMobile());
        }
        if (req.getStatus() != null) {
            a.setStatus(req.getStatus());
        }
        accountMapper.updateById(a);
    }

    /**
     * 重置账号密码
     *
     * 为指定账号设置新的密码
     *
     * @param req 密码重置请求对象，包含账号ID和新密码
     * @throws BusinessException 当账号不存在或管理员权限验证失败时抛出异常
     */
    public void resetPassword(AccountResetPasswordRequest req) {
        checkAdmin();
        SysAccount a = accountMapper.selectById(req.getId());
        if (a == null || a.getDelFlag() != null && a.getDelFlag() != 0) {
            throw new BusinessException(404, "账号不存在");
        }
        a.setPassword(PasswordUtil.encode(req.getNewPassword()));
        accountMapper.updateById(a);
    }

    /**
     * 删除指定账号
     *
     * 根据账号ID执行删除操作（软删除）
     *
     * @param id 需要删除的账号唯一标识符
     * @throws BusinessException 当管理员权限验证失败时抛出异常
     */
    public void delete(String id) {
        checkAdmin();
        accountMapper.deleteById(id);
    }

    /**
     * 将SysAccount实体转换为AccountVO视图对象
     *
     * 私有方法，用于将数据库实体对象转换为对外提供的视图对象
     *
     * @param a 系统账号实体对象
     * @return 账号视图对象，仅包含对外公开的字段
     */
    private AccountVO toVO(SysAccount a) {
        AccountVO vo = new AccountVO();
        vo.setId(a.getId());
        vo.setUsername(a.getUsername());
        vo.setMobile(a.getMobile());
        vo.setStatus(a.getStatus());
        return vo;
    }
}

