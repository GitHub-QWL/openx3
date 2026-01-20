package com.openx3.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openx3.common.exception.BusinessException;
import com.openx3.common.utils.PasswordUtil;
import com.openx3.system.domain.dto.LoginRequest;
import com.openx3.system.domain.dto.SwitchContextRequest;
import com.openx3.system.domain.model.AuthTokenContext;
import com.openx3.system.domain.model.BpContext;
import com.openx3.system.domain.vo.*;
import com.openx3.system.entity.SysRole;
import com.openx3.system.entity.iam.SysAccount;
import com.openx3.system.entity.iam.SysDept;
import com.openx3.system.entity.iam.SysEmployee;
import com.openx3.system.entity.iam.SysTenant;
import com.openx3.system.entity.iam.SysPost;
import com.openx3.system.entity.relation.SysRoleDept;
import com.openx3.system.mapper.*;
import com.openx3.system.security.AuthContextHolder;
import com.openx3.system.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证服务
 * 负责登录、登出、Token管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysAccountMapper accountMapper;
    private final SysEmployeeMapper employeeMapper;
    private final SysTenantMapper tenantMapper;
    private final SysDeptMapper deptMapper;
    private final IamRbacService rbacService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SysRoleDeptMapper roleDeptMapper;

    /**
     * 第一步：账号登录（不签发 Token，仅返回员工列表供选择上下文）
     */
    public AccountLoginVO login(LoginRequest request) {
        SysAccount account = checkAccountPassword(request.getAccount(), request.getPassword());
        List<SysEmployee> employees = employeeMapper.selectList(new LambdaQueryWrapper<SysEmployee>()
                .eq(SysEmployee::getAccountId, account.getId()));

        AccountLoginVO vo = new AccountLoginVO();
        vo.setAccountId(account.getId());
        vo.setEmployees(buildEmployeeContexts(employees));
        return vo;
    }

    /**
     * 第二步：选择业务上下文并签发 Token
     * 说明：为了严格无状态，不依赖服务端临时 session，这里再次校验账号密码。
     */
    public LoginVO select(LoginRequest request) {
        if (!StringUtils.hasText(request.getEmployeeId())) {
            throw new BusinessException(400, "employeeId不能为空");
        }

        SysAccount account = checkAccountPassword(request.getAccount(), request.getPassword());
        SysEmployee employee = employeeMapper.selectById(request.getEmployeeId());
        if (employee == null || !Objects.equals(employee.getAccountId(), account.getId())) {
            throw new BusinessException(403, "无权选择该员工身份");
        }

        // 登录（loginId 使用 AccountId）
        StpUtil.login(account.getId());

        AuthTokenContext ctx = buildTokenContext(account, employee);
        AuthContextHolder.set(ctx);

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setAccountId(account.getId());
        vo.setEmployeeId(employee.getId());
        vo.setDisplayName(StringUtils.hasText(employee.getRealName()) ? employee.getRealName() : account.getUsername());
        return vo;
    }

    /**
     * 退出登录
     * （下一步会接入 Redis 黑名单，做到可控强制注销）
     */
    public void logout() {
        tokenBlacklistService.blacklistCurrentToken();
        StpUtil.logout();
    }

    /**
     * 获取当前登录态信息（用户 + 角色 + 权限 + 菜单）
     */
    public AuthInfoVO getCurrentAuthInfo() {
        StpUtil.checkLogin();
        AuthTokenContext ctx = AuthContextHolder.getRequired();

        SysAccount account = accountMapper.selectById(ctx.getSub());
        if (account == null) {
            throw new BusinessException(404, "账号不存在");
        }

        EmployeeContextVO empVo = buildEmployeeContext(ctx.getBpContext() != null ? ctx.getBpContext().getUid() : null);

        AuthInfoVO vo = new AuthInfoVO();
        vo.setAccount(toAccountVO(account));
        vo.setEmployee(empVo);
        vo.setRoles(rbacService.listRoleCodes(ctx));
        vo.setPermissions(rbacService.listAuthorities(ctx));
        vo.setDsScope(ctx.getDsScope());
        vo.setMenus(rbacService.buildMenuTree(ctx));
        return vo;
    }

    /**
     * 切换业务身份（签发新 Token）
     */
    public LoginVO switchContext(SwitchContextRequest request) {
        StpUtil.checkLogin();
        String accountId = StpUtil.getLoginIdAsString();

        SysEmployee employee = employeeMapper.selectById(request.getEmployeeId());
        if (employee == null || !Objects.equals(employee.getAccountId(), accountId)) {
            throw new BusinessException(403, "无权切换到该员工身份");
        }

        SysAccount account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BusinessException(404, "账号不存在");
        }

        // 旧 Token 加入黑名单，然后签发新 Token
        tokenBlacklistService.blacklistCurrentToken();
        StpUtil.logout();
        StpUtil.login(accountId);

        AuthTokenContext ctx = buildTokenContext(account, employee);
        AuthContextHolder.set(ctx);

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setAccountId(accountId);
        vo.setEmployeeId(employee.getId());
        vo.setDisplayName(StringUtils.hasText(employee.getRealName()) ? employee.getRealName() : account.getUsername());
        return vo;
    }

    private SysAccount checkAccountPassword(String accountOrMobile, String password) {
        SysAccount account = accountMapper.selectOne(new LambdaQueryWrapper<SysAccount>()
                .eq(SysAccount::getDelFlag, 0)
                .and(q -> q.eq(SysAccount::getUsername, accountOrMobile).or().eq(SysAccount::getMobile, accountOrMobile)));

        if (account == null) {
            throw new BusinessException(400, "账号或密码错误");
        }
        if (Integer.valueOf(0).equals(account.getStatus())) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!PasswordUtil.matches(password, account.getPassword())) {
            throw new BusinessException(400, "账号或密码错误");
        }
        return account;
    }

    private List<EmployeeContextVO> buildEmployeeContexts(List<SysEmployee> employees) {
        if (employees == null || employees.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> tenantIds = employees.stream().map(SysEmployee::getTenantId).filter(StringUtils::hasText).collect(Collectors.toSet());
        Set<String> deptIds = employees.stream().map(SysEmployee::getDeptId).filter(StringUtils::hasText).collect(Collectors.toSet());

        Map<String, SysTenant> tenantMap = tenantIds.isEmpty() ? Map.of() : tenantMapper.selectList(new LambdaQueryWrapper<SysTenant>()
                .in(SysTenant::getId, tenantIds)
                .eq(SysTenant::getDelFlag, 0)).stream().collect(Collectors.toMap(SysTenant::getId, t -> t, (a, b) -> a));

        Map<String, SysDept> deptMap = deptIds.isEmpty() ? Map.of() : deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .in(SysDept::getId, deptIds)
                .eq(SysDept::getDelFlag, 0)).stream().collect(Collectors.toMap(SysDept::getId, d -> d, (a, b) -> a));

        List<EmployeeContextVO> list = new ArrayList<>();
        for (SysEmployee e : employees) {
            EmployeeContextVO vo = new EmployeeContextVO();
            vo.setEmployeeId(e.getId());
            vo.setRealName(e.getRealName());
            vo.setIsMain(e.getIsMain());
            vo.setTenantId(e.getTenantId());
            vo.setDeptId(e.getDeptId());

            SysTenant t = tenantMap.get(e.getTenantId());
            if (t != null) vo.setTenantName(t.getName());
            SysDept d = deptMap.get(e.getDeptId());
            if (d != null) vo.setDeptName(d.getDeptName());

            List<SysPost> posts = rbacService.listPostsByEmployeeId(e.getId());
            vo.setPostCodes(posts.stream().map(SysPost::getPostCode).collect(Collectors.toList()));
            vo.setPostNames(posts.stream().map(SysPost::getPostName).collect(Collectors.toList()));

            list.add(vo);
        }
        return list;
    }

    private EmployeeContextVO buildEmployeeContext(String employeeId) {
        if (!StringUtils.hasText(employeeId)) return null;
        SysEmployee e = employeeMapper.selectById(employeeId);
        if (e == null) return null;
        return buildEmployeeContexts(List.of(e)).stream().findFirst().orElse(null);
    }

    private AccountVO toAccountVO(SysAccount a) {
        AccountVO vo = new AccountVO();
        vo.setId(a.getId());
        vo.setUsername(a.getUsername());
        vo.setMobile(a.getMobile());
        vo.setStatus(a.getStatus());
        return vo;
    }

    private AuthTokenContext buildTokenContext(SysAccount account, SysEmployee employee) {
        AuthTokenContext ctx = new AuthTokenContext();
        ctx.setSub(account.getId());
        ctx.setJti(UUID.randomUUID().toString());

        BpContext bp = new BpContext();
        bp.setUid(employee.getId());
        bp.setTid(employee.getTenantId());
        bp.setDeptId(employee.getDeptId());
        bp.setPosts(rbacService.listPostsByEmployeeId(employee.getId()).stream().map(SysPost::getPostCode).collect(Collectors.toList()));
        ctx.setBpContext(bp);

        // 计算快照（角色/权限/数据范围）
        List<SysRole> roles = rbacService.listRoles(ctx);
        ctx.setRoleCodes(roles.stream().map(SysRole::getCode).filter(StringUtils::hasText).collect(Collectors.toCollection(LinkedHashSet::new)));
        ctx.setAuthorities(rbacService.listAuthorities(ctx));

        String dsScope = rbacService.calcMaxDataScope(ctx);
        ctx.setDsScope(dsScope);

        // CUSTOM 数据权限：写入允许访问的部门列表，供 SQL 拦截器使用
        if ("CUSTOM".equalsIgnoreCase(dsScope) && !roles.isEmpty()) {
            Set<String> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toSet());
            List<SysRoleDept> rels = roleDeptMapper.selectList(new LambdaQueryWrapper<SysRoleDept>()
                    .in(SysRoleDept::getRoleId, roleIds)
                    .eq(SysRoleDept::getDelFlag, 0));
            for (SysRoleDept r : rels) {
                if (StringUtils.hasText(r.getDeptId())) {
                    ctx.getCustomDeptIds().add(r.getDeptId());
                }
            }
        }
        return ctx;
    }
}