-- ===================================================================
-- OpenX3 初始化数据脚本
-- 适用版本: V1.2.0 (Schema 架构版)
-- 说明:
-- 1. 初始化 菜单 (Menu)
-- 2. 初始化 角色 (Role) & 授权
-- 3. [核心] 元数据自举：将系统表注册为业务对象
-- ===================================================================

SET search_path TO openx3;

-- -------------------------------------------------------------------
-- 0. 租户/组织/岗位/账号/员工初始化（双层身份隔离）
-- -------------------------------------------------------------------
DELETE FROM cfg_local_menu;
DELETE FROM cfg_param;
DELETE FROM cfg_dict_item;
DELETE FROM cfg_dict;

DELETE FROM sys_employee_post;
DELETE FROM sys_employee;
DELETE FROM sys_post;
DELETE FROM sys_dept;
DELETE FROM sys_tenant;
DELETE FROM sys_account;
DELETE FROM sys_client;

-- 默认租户
INSERT INTO sys_tenant (id, name, create_time, create_by)
VALUES ('tenant-default', '默认租户', NOW(), 'system');

-- 根部门
INSERT INTO sys_dept (id, tenant_id, parent_id, dept_name, tree_path, sort_no, status, create_time, create_by)
VALUES ('dept-root', 'tenant-default', '0', '总部', 'dept-root', 0, 1, NOW(), 'system');

-- 系统管理部（示例）
INSERT INTO sys_dept (id, tenant_id, parent_id, dept_name, tree_path, sort_no, status, create_time, create_by)
VALUES ('dept-it', 'tenant-default', 'dept-root', '系统管理部', 'dept-root,dept-it', 10, 1, NOW(), 'system');

-- 岗位：超级管理员 / 开发者
INSERT INTO sys_post (id, tenant_id, post_code, post_name, status, create_time, create_by)
VALUES
    ('post-admin', 'tenant-default', 'SYS_ADMIN', '超级管理员', 1, NOW(), 'system'),
    ('post-dev',   'tenant-default', 'DEV',       '开发者',     1, NOW(), 'system');

-- 账号：admin（密码: 123456）
INSERT INTO sys_account (id, username, mobile, password, salt, status, create_time, create_by, del_flag, version)
VALUES ('acc-admin', 'admin', '13800138000', '$2a$10$7JB720yubVSZv5W8vNGkarOu7zyyWW.M0Y.Dop.tS/h.w.w.w.w.', NULL, 1, NOW(), 'system', 0, 1);

-- 员工：admin 在默认租户的业务身份（uid）
INSERT INTO sys_employee (id, account_id, tenant_id, dept_id, emp_no, real_name, is_main, create_time, create_by, del_flag, version)
VALUES ('emp-admin-001', 'acc-admin', 'tenant-default', 'dept-root', '0001', '管理员', TRUE, NOW(), 'system', 0, 1);

-- 绑定岗位
INSERT INTO sys_employee_post (id, employee_id, post_id, create_time, create_by)
VALUES
    ('ep-admin-001', 'emp-admin-001', 'post-admin', NOW(), 'system');

-- -------------------------------------------------------------------
-- 1. 系统角色 (sys_role)
-- -------------------------------------------------------------------
DELETE FROM sys_role;

-- 超级管理员
INSERT INTO sys_role (id, role_code, role_name, data_scope, description, create_time, create_by)
VALUES ('role-admin', 'admin', '超级管理员', 'ALL', '拥有系统最高权限', NOW(), 'system');

-- 开发者
INSERT INTO sys_role (id, role_code, role_name, data_scope, description, create_time, create_by)
VALUES ('role-dev', 'dev', '开发者', 'DEPT_AND_CHILD', '具备元数据配置和脚本开发权限', NOW(), 'system');


-- -------------------------------------------------------------------
-- 2. 部门/岗位绑定角色（用户不得直接绑定角色）
-- -------------------------------------------------------------------
DELETE FROM sys_dept_role;
DELETE FROM sys_post_role;

-- IT 部门基础权限：开发者
INSERT INTO sys_dept_role (id, dept_id, role_id, create_time, create_by)
VALUES ('dr-it-dev', 'dept-it', 'role-dev', NOW(), 'system');

-- 超级管理员岗位：admin 角色
INSERT INTO sys_post_role (id, post_id, role_id, create_time, create_by)
VALUES ('pr-admin', 'post-admin', 'role-admin', NOW(), 'system');


-- -------------------------------------------------------------------
-- 3. 系统菜单 (sys_menu)
-- ID 策略：使用易读字符串 (如 menu-sys) 以便维护，生产环境可用 UUID
-- -------------------------------------------------------------------
DELETE FROM sys_menu;

-- [一级] 系统管理
INSERT INTO sys_menu (id, parent_id, title, path, component, icon, type, sort_no, perms, create_time)
VALUES ('menu-sys', '0', '系统管理', '/system', 'Layout', 'setting', 0, 10, NULL, NOW());

-- [二级] 用户管理
INSERT INTO sys_menu (id, parent_id, title, path, component, icon, type, sort_no, perms, create_time)
VALUES ('menu-sys-user', 'menu-sys', '用户管理', 'user/index', 'system/user/index', 'user', 1, 10, 'sys:user:list', NOW());

-- [二级] 角色管理
INSERT INTO sys_menu (id, parent_id, title, path, component, icon, type, sort_no, perms, create_time)
VALUES ('menu-sys-role', 'menu-sys', '角色管理', 'role/index', 'system/role/index', 'team', 1, 20, 'sys:role:list', NOW());

-- [二级] 菜单管理
INSERT INTO sys_menu (id, parent_id, title, path, component, icon, type, sort_no, perms, create_time)
VALUES ('menu-sys-menu', 'menu-sys', '菜单管理', 'menu/index', 'system/menu/index', 'bars', 1, 30, 'sys:menu:list', NOW());

-- [一级] 低代码引擎 (Dev)
INSERT INTO sys_menu (id, parent_id, title, path, component, icon, type, sort_no, perms, create_time)
VALUES ('menu-dev', '0', '低代码引擎', '/dev', 'Layout', 'rocket', 0, 20, NULL, NOW());

-- [二级] 业务对象 (Core)
INSERT INTO sys_menu (id, parent_id, title, path, component, icon, type, sort_no, perms, create_time)
VALUES ('menu-dev-obj', 'menu-dev', '业务对象', 'object/index', 'dev/object/index', 'database', 1, 10, 'dev:obj:list', NOW());

-- [二级] 脚本管理 (Script)
INSERT INTO sys_menu (id, parent_id, title, path, component, icon, type, sort_no, perms, create_time)
VALUES ('menu-dev-script', 'menu-dev', '脚本管理', 'script/index', 'dev/script/index', 'code', 1, 20, 'dev:script:list', NOW());


-- -------------------------------------------------------------------
-- 4. 角色权限绑定 (sys_role_menu)
-- 给 Admin 赋予所有权限
-- -------------------------------------------------------------------
DELETE FROM sys_role_menu;

-- 简单脚本：将所有菜单赋予 role-admin
INSERT INTO sys_role_menu (id, role_id, menu_id, create_time, create_by)
SELECT
    'rm-' || id, -- 生成唯一ID
    'role-admin',
    id,
    NOW(),
    'system'
FROM sys_menu;


-- -------------------------------------------------------------------
-- 4.1 运行时权限码初始化 (sys_permission + sys_role_permission)
-- 说明：运行时接口权限格式 AUTH_{OBJECT}_{ACTION}
-- -------------------------------------------------------------------
DELETE FROM sys_permission;
DELETE FROM sys_role_permission;

-- SYS_ACCOUNT（账号）
INSERT INTO sys_permission (id, code, name, object_code, action, description, create_time, create_by)
VALUES
    ('perm-sys-acc-query',  'AUTH_SYS_ACCOUNT_QUERY',  '账号-查询', 'SYS_ACCOUNT', 'QUERY',  '运行时查询 SYS_ACCOUNT', NOW(), 'system'),
    ('perm-sys-acc-save',   'AUTH_SYS_ACCOUNT_SAVE',   '账号-保存', 'SYS_ACCOUNT', 'SAVE',   '运行时保存 SYS_ACCOUNT', NOW(), 'system'),
    ('perm-sys-acc-delete', 'AUTH_SYS_ACCOUNT_DELETE', '账号-删除', 'SYS_ACCOUNT', 'DELETE', '运行时删除 SYS_ACCOUNT', NOW(), 'system');

-- SYS_EMPLOYEE（员工）
INSERT INTO sys_permission (id, code, name, object_code, action, description, create_time, create_by)
VALUES
    ('perm-sys-emp-query',  'AUTH_SYS_EMPLOYEE_QUERY',  '员工-查询', 'SYS_EMPLOYEE', 'QUERY',  '运行时查询 SYS_EMPLOYEE', NOW(), 'system'),
    ('perm-sys-emp-save',   'AUTH_SYS_EMPLOYEE_SAVE',   '员工-保存', 'SYS_EMPLOYEE', 'SAVE',   '运行时保存 SYS_EMPLOYEE', NOW(), 'system'),
    ('perm-sys-emp-delete', 'AUTH_SYS_EMPLOYEE_DELETE', '员工-删除', 'SYS_EMPLOYEE', 'DELETE', '运行时删除 SYS_EMPLOYEE', NOW(), 'system');

-- SYS_OBJECT
INSERT INTO sys_permission (id, code, name, object_code, action, description, create_time, create_by)
VALUES
    ('perm-sys-obj-query',  'AUTH_SYS_OBJECT_QUERY',  '业务对象-查询', 'SYS_OBJECT', 'QUERY',  '运行时查询 SYS_OBJECT', NOW(), 'system'),
    ('perm-sys-obj-save',   'AUTH_SYS_OBJECT_SAVE',   '业务对象-保存', 'SYS_OBJECT', 'SAVE',   '运行时保存 SYS_OBJECT', NOW(), 'system'),
    ('perm-sys-obj-delete', 'AUTH_SYS_OBJECT_DELETE', '业务对象-删除', 'SYS_OBJECT', 'DELETE', '运行时删除 SYS_OBJECT', NOW(), 'system');

-- 给 role-admin 赋予所有运行时权限码
INSERT INTO sys_role_permission (id, role_id, permission_id, create_time, create_by)
SELECT
    'rp-' || id,
    'role-admin',
    id,
    NOW(),
    'system'
FROM sys_permission;


-- -------------------------------------------------------------------
-- 5. [Core] 元数据自举 (Bootstrapping)
-- 将系统表注册到 sys_object，演示“元数据驱动”
-- -------------------------------------------------------------------
DELETE FROM sys_object;
DELETE FROM sys_field;

-- 5.1 注册 sys_account 对象（认证层）
INSERT INTO sys_object (id, code, name, table_name, is_audit, store_type, remark, create_time, create_by)
VALUES ('obj-sys-acc', 'SYS_ACCOUNT', '账号', 'sys_account', true, 1, '认证层账号表', NOW(), 'system');

-- 5.2 注册 sys_account 的字段
INSERT INTO sys_field (id, object_code, field_name, field_label, field_type, widget_type, is_required, sort_no, create_time)
VALUES
    ('f-acc-username', 'SYS_ACCOUNT', 'username', '登录账号', 'String', 'Input', true, 10, NOW()),
    ('f-acc-mobile',   'SYS_ACCOUNT', 'mobile',   '手机号',   'String', 'Input', false, 20, NOW()),
    ('f-acc-status',   'SYS_ACCOUNT', 'status',   '状态',     'Integer', 'Select', true, 30, NOW());

-- 5.3 注册 sys_employee 对象（授权层）
INSERT INTO sys_object (id, code, name, table_name, is_audit, store_type, remark, create_time, create_by)
VALUES ('obj-sys-emp', 'SYS_EMPLOYEE', '员工', 'sys_employee', true, 1, '授权层员工表', NOW(), 'system');

INSERT INTO sys_field (id, object_code, field_name, field_label, field_type, widget_type, is_required, sort_no, create_time)
VALUES
    ('f-emp-realname', 'SYS_EMPLOYEE', 'real_name', '姓名', 'String', 'Input', true, 10, NOW()),
    ('f-emp-empno',    'SYS_EMPLOYEE', 'emp_no',    '工号', 'String', 'Input', false, 20, NOW()),
    ('f-emp-dept',     'SYS_EMPLOYEE', 'dept_id',   '部门', 'String', 'Select', false, 30, NOW());

-- 5.3 注册 sys_object 自身 (这一点很酷：系统可以管理“对象定义”这个对象)
INSERT INTO sys_object (id, code, name, table_name, is_audit, store_type, remark, create_time, create_by)
VALUES ('obj-sys-obj', 'SYS_OBJECT', '业务对象', 'sys_object', true, 1, '元数据核心表', NOW(), 'system');


-- -------------------------------------------------------------------
-- 6. 字段权限策略示例 (sys_field_policy)
-- -------------------------------------------------------------------
DELETE FROM sys_field_policy;

-- 示例：dev 角色在 sys_account 列表中隐藏 mobile；admin 不限制
INSERT INTO sys_field_policy (id, role_id, resource_code, field_name, policy, policy_param, create_time, create_by)
VALUES ('fp-dev-hide-mobile', 'role-dev', 'sys_account_list', 'mobile', 'HIDDEN', NULL, NOW(), 'system');


-- -------------------------------------------------------------------
-- 7. 配置/常量（cfg_*）示例（参考 Sage X3：字典、参数、本地菜单）
-- -------------------------------------------------------------------

-- 字典：性别
INSERT INTO cfg_dict (id, dict_code, dict_name, status, remark, create_time, create_by)
VALUES ('dict-gender', 'GENDER', '性别', 1, '示例字典', NOW(), 'system');

INSERT INTO cfg_dict_item (id, dict_id, item_value, item_label, sort_no, status, create_time, create_by)
VALUES
    ('dict-item-gender-1', 'dict-gender', '1', '男', 10, 1, NOW(), 'system'),
    ('dict-item-gender-2', 'dict-gender', '2', '女', 20, 1, NOW(), 'system');

-- 参数：是否启用本地菜单
INSERT INTO cfg_param (id, param_code, param_name, param_value, remark, create_time, create_by)
VALUES ('param-local-menu', 'LOCAL_MENU_ENABLED', '是否启用本地菜单', 'true', '示例参数', NOW(), 'system');

-- 本地菜单：对接菜单（示例）
INSERT INTO cfg_local_menu (id, parent_id, title, function_code, url, icon, sort_no, status, create_time, create_by)
VALUES
    ('lm-root', '0', '对接菜单', NULL, NULL, 'link', 10, 1, NOW(), 'system'),
    ('lm-sagex3', 'lm-root', 'Sage X3', 'SAGEX3', 'https://sagex3.example.com', 'external-link', 20, 1, NOW(), 'system');