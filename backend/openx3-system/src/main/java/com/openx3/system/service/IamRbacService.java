package com.openx3.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openx3.system.domain.model.AuthTokenContext;
import com.openx3.system.domain.model.BpContext;
import com.openx3.system.domain.vo.MenuVO;
import com.openx3.system.entity.SysMenu;
import com.openx3.system.entity.SysPermission;
import com.openx3.system.entity.SysRole;
import com.openx3.system.entity.iam.SysDept;
import com.openx3.system.entity.iam.SysPost;
import com.openx3.system.entity.relation.*;
import com.openx3.system.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * IAM RBAC 权限计算服务（严格遵循文档）
 * - 权限只绑定到角色
 * - 用户/员工不得直接绑定角色
 * - 员工通过 部门/岗位 间接获得角色
 */
@Service
@RequiredArgsConstructor
public class IamRbacService {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysPermissionMapper permissionMapper;

    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;
    private final SysEmployeePostMapper employeePostMapper;
    private final SysDeptRoleMapper deptRoleMapper;
    private final SysPostRoleMapper postRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public Set<String> listRoleCodes(AuthTokenContext ctx) {
        return listRoles(ctx).stream()
                .map(SysRole::getCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<SysRole> listRoles(AuthTokenContext ctx) {
        BpContext bp = ctx.getBpContext();
        if (bp == null || !StringUtils.hasText(bp.getUid())) {
            return Collections.emptyList();
        }

        Set<String> roleIds = new LinkedHashSet<>();

        // 1) 部门角色：包含树路径上的所有部门（继承）
        if (StringUtils.hasText(bp.getDeptId())) {
            SysDept dept = deptMapper.selectById(bp.getDeptId());
            if (dept != null && StringUtils.hasText(dept.getTreePath())) {
                Set<String> deptIds = Arrays.stream(dept.getTreePath().split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (!deptIds.isEmpty()) {
                    List<SysDeptRole> rels = deptRoleMapper.selectList(new LambdaQueryWrapper<SysDeptRole>()
                            .in(SysDeptRole::getDeptId, deptIds)
                            .eq(SysDeptRole::getDelFlag, 0));
                    for (SysDeptRole r : rels) {
                        if (StringUtils.hasText(r.getRoleId())) {
                            roleIds.add(r.getRoleId());
                        }
                    }
                }
            } else {
                // tree_path 缺失时，至少取本部门绑定
                List<SysDeptRole> rels = deptRoleMapper.selectList(new LambdaQueryWrapper<SysDeptRole>()
                        .eq(SysDeptRole::getDeptId, bp.getDeptId())
                        .eq(SysDeptRole::getDelFlag, 0));
                for (SysDeptRole r : rels) {
                    if (StringUtils.hasText(r.getRoleId())) {
                        roleIds.add(r.getRoleId());
                    }
                }
            }
        }

        // 2) 岗位角色
        List<SysEmployeePost> eps = employeePostMapper.selectList(new LambdaQueryWrapper<SysEmployeePost>()
                .eq(SysEmployeePost::getEmployeeId, bp.getUid())
                .eq(SysEmployeePost::getDelFlag, 0));
        if (eps != null && !eps.isEmpty()) {
            Set<String> postIds = eps.stream()
                    .map(SysEmployeePost::getPostId)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            if (!postIds.isEmpty()) {
                List<SysPostRole> rels = postRoleMapper.selectList(new LambdaQueryWrapper<SysPostRole>()
                        .in(SysPostRole::getPostId, postIds)
                        .eq(SysPostRole::getDelFlag, 0));
                for (SysPostRole r : rels) {
                    if (StringUtils.hasText(r.getRoleId())) {
                        roleIds.add(r.getRoleId());
                    }
                }
            }
        }

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getDelFlag, 0));
    }

    public String calcMaxDataScope(AuthTokenContext ctx) {
        List<SysRole> roles = listRoles(ctx);
        if (roles.isEmpty()) {
            return "SELF";
        }

        // 从最宽到最窄排序
        List<String> order = List.of("ALL", "DEPT_AND_CHILD", "DEPT", "SELF", "CUSTOM");
        String best = "SELF";
        int bestIdx = order.indexOf(best);

        for (SysRole r : roles) {
            String ds = r.getDataScope();
            if (!StringUtils.hasText(ds)) continue;
            int idx = order.indexOf(ds);
            if (idx != -1 && idx < bestIdx) {
                best = ds;
                bestIdx = idx;
            }
        }
        return best;
    }

    public Set<String> listAuthorities(AuthTokenContext ctx) {
        Set<String> result = new LinkedHashSet<>();

        List<SysRole> roles = listRoles(ctx);
        if (roles.isEmpty()) {
            return result;
        }
        Set<String> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toSet());

        // 1) 菜单权限（sys_menu.perms） via sys_role_menu
        List<SysRoleMenu> rms = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds)
                .eq(SysRoleMenu::getDelFlag, 0));
        if (rms != null && !rms.isEmpty()) {
            Set<String> menuIds = rms.stream().map(SysRoleMenu::getMenuId).filter(StringUtils::hasText).collect(Collectors.toSet());
            if (!menuIds.isEmpty()) {
                List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .eq(SysMenu::getDelFlag, 0));
                for (SysMenu m : menus) {
                    if (StringUtils.hasText(m.getPerms())) {
                        result.add(m.getPerms());
                    }
                }
            }
        }

        // 2) API/运行时权限码（sys_permission.code） via sys_role_permission
        List<SysRolePermission> rps = rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                .in(SysRolePermission::getRoleId, roleIds)
                .eq(SysRolePermission::getDelFlag, 0));
        if (rps != null && !rps.isEmpty()) {
            Set<String> permIds = rps.stream().map(SysRolePermission::getPermissionId).filter(StringUtils::hasText).collect(Collectors.toSet());
            if (!permIds.isEmpty()) {
                List<SysPermission> perms = permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getId, permIds)
                        .eq(SysPermission::getDelFlag, 0));
                for (SysPermission p : perms) {
                    if (StringUtils.hasText(p.getCode())) {
                        result.add(p.getCode());
                    }
                }
            }
        }

        return result;
    }

    public List<MenuVO> buildMenuTree(AuthTokenContext ctx) {
        List<SysRole> roles = listRoles(ctx);
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toSet());

        List<SysRoleMenu> rms = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds)
                .eq(SysRoleMenu::getDelFlag, 0));
        if (rms == null || rms.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> menuIds = rms.stream().map(SysRoleMenu::getMenuId).filter(StringUtils::hasText).collect(Collectors.toSet());
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 仅返回目录/菜单，按钮留给 perms 做前端控制
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .eq(SysMenu::getDelFlag, 0))
                .stream()
                .filter(m -> m.getType() == null || m.getType() != 2)
                .filter(m -> m.getVisible() == null || Boolean.TRUE.equals(m.getVisible()))
                .collect(Collectors.toList());

        Map<String, MenuVO> map = menus.stream()
                .map(this::toMenuVO)
                .collect(Collectors.toMap(MenuVO::getId, Function.identity(), (a, b) -> a));

        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO node : map.values()) {
            String pid = node.getParentId();
            if (!StringUtils.hasText(pid) || "0".equals(pid) || !map.containsKey(pid)) {
                roots.add(node);
            } else {
                map.get(pid).getChildren().add(node);
            }
        }

        sortMenuTree(roots);
        return roots;
    }

    public List<SysPost> listPostsByEmployeeId(String employeeId) {
        List<SysEmployeePost> eps = employeePostMapper.selectList(new LambdaQueryWrapper<SysEmployeePost>()
                .eq(SysEmployeePost::getEmployeeId, employeeId)
                .eq(SysEmployeePost::getDelFlag, 0));
        if (eps == null || eps.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> postIds = eps.stream().map(SysEmployeePost::getPostId).filter(StringUtils::hasText).collect(Collectors.toSet());
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postMapper.selectList(new LambdaQueryWrapper<SysPost>()
                .in(SysPost::getId, postIds)
                .eq(SysPost::getDelFlag, 0));
    }

    private void sortMenuTree(List<MenuVO> nodes) {
        if (nodes == null) return;
        nodes.sort(Comparator.comparing(MenuVO::getSortNo, Comparator.nullsLast(Integer::compareTo)));
        for (MenuVO n : nodes) {
            sortMenuTree(n.getChildren());
        }
    }

    private MenuVO toMenuVO(SysMenu m) {
        MenuVO vo = new MenuVO();
        vo.setId(m.getId());
        vo.setParentId(m.getParentId());
        vo.setTitle(m.getTitle());
        vo.setPath(m.getPath());
        vo.setComponent(m.getComponent());
        vo.setIcon(m.getIcon());
        vo.setType(m.getType());
        vo.setSortNo(m.getSortNo());
        vo.setVisible(m.getVisible());
        vo.setPerms(m.getPerms());
        return vo;
    }
}

