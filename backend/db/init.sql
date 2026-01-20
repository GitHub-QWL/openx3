-- ===================================================================
-- OpenX3 核心数据库 Schema (PostgreSQL)
-- 版本: V1.2.0 (多租户 Schema 架构版)
-- 说明:
-- 1. 默认创建并使用 'openx3' Schema，作为主租户或模板租户
-- 2. 后续新租户可直接复制此 Schema 结构 (如 tenant_001, tenant_002)
-- 3. 所有表统一包含审计字段，ID 统一为 VARCHAR(36)
-- ===================================================================

-- -------------------------------------------------------------------
-- [Schema Initialization] 模式初始化
-- -------------------------------------------------------------------

-- 1. 创建 openx3 模式 (如果不存在)
CREATE SCHEMA IF NOT EXISTS openx3;
COMMENT ON SCHEMA openx3 IS 'OpenX3 基础平台默认模式 (标准模板)';

-- 2. 设置当前会话的搜索路径
-- 这一步至关重要：确保后续创建的表都位于 openx3 模式下，而非 public
SET search_path TO openx3;


-- -------------------------------------------------------------------
-- [Core Module] 内核引擎表 (位于 openx3 模式下)
-- -------------------------------------------------------------------

-- 1. 动态脚本表 (sys_script)
DROP TABLE IF EXISTS sys_script;
CREATE TABLE sys_script (
                            id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                            code        VARCHAR(100) NOT NULL,
                            object_code VARCHAR(100),
                            event       VARCHAR(50),
                            content     TEXT,
                            is_active   BOOLEAN      DEFAULT TRUE,

    -- 审计字段
                            tenant_id   VARCHAR(50)  DEFAULT '000000',
                            create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            create_by   VARCHAR(64),
                            update_by   VARCHAR(64),
                            del_flag    SMALLINT     DEFAULT 0,
                            version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_script IS 'Groovy 动态脚本表';
COMMENT ON COLUMN sys_script.id IS '主键ID';
COMMENT ON COLUMN sys_script.code IS '脚本编码 (Unique, 如 SOH_SAVE)';
COMMENT ON COLUMN sys_script.object_code IS '关联业务对象编码';
COMMENT ON COLUMN sys_script.event IS '触发事件 (SAVE, QUERY, API)';
COMMENT ON COLUMN sys_script.content IS 'Groovy 源代码';
COMMENT ON COLUMN sys_script.is_active IS '是否启用';
COMMENT ON COLUMN sys_script.create_time IS '创建时间';
COMMENT ON COLUMN sys_script.update_time IS '更新时间';
COMMENT ON COLUMN sys_script.create_by IS '创建人';
COMMENT ON COLUMN sys_script.update_by IS '更新人';
COMMENT ON COLUMN sys_script.del_flag IS '逻辑删除 (0:正常, 1:删除)';
COMMENT ON COLUMN sys_script.version IS '乐观锁版本号';

CREATE UNIQUE INDEX idx_sys_script_code ON sys_script(code) WHERE del_flag = 0;


-- 2. 业务对象元数据表 (sys_object)
DROP TABLE IF EXISTS sys_object;
CREATE TABLE sys_object (
                            id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                            code        VARCHAR(100) NOT NULL,
                            name        VARCHAR(200),
                            table_name  VARCHAR(200),
                            std_script  VARCHAR(100),
                            spe_script  VARCHAR(100),
                            is_audit    BOOLEAN      DEFAULT TRUE,
                            store_type  INTEGER      DEFAULT 1,
                            remark      VARCHAR(500),

    -- 审计字段
                            tenant_id   VARCHAR(50)  DEFAULT '000000',
                            create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            create_by   VARCHAR(64),
                            update_by   VARCHAR(64),
                            del_flag    SMALLINT     DEFAULT 0,
                            version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_object IS '业务对象元数据';
COMMENT ON COLUMN sys_object.id IS '主键ID';
COMMENT ON COLUMN sys_object.code IS '对象编码 (Unique, 如 SOH)';
COMMENT ON COLUMN sys_object.name IS '对象名称 (如 销售订单)';
COMMENT ON COLUMN sys_object.table_name IS '物理表名';
COMMENT ON COLUMN sys_object.std_script IS '标准逻辑脚本 (Standard)';
COMMENT ON COLUMN sys_object.spe_script IS '二开逻辑脚本 (Specific)';
COMMENT ON COLUMN sys_object.is_audit IS '是否开启自动审计';
COMMENT ON COLUMN sys_object.store_type IS '存储类型 (1=物理表, 2=混合模式)';
COMMENT ON COLUMN sys_object.remark IS '备注';
COMMENT ON COLUMN sys_object.create_time IS '创建时间';
COMMENT ON COLUMN sys_object.update_time IS '更新时间';
COMMENT ON COLUMN sys_object.create_by IS '创建人';
COMMENT ON COLUMN sys_object.update_by IS '更新人';
COMMENT ON COLUMN sys_object.del_flag IS '逻辑删除';
COMMENT ON COLUMN sys_object.version IS '乐观锁版本号';

CREATE UNIQUE INDEX idx_sys_object_code ON sys_object(code) WHERE del_flag = 0;


-- 3. 字段元数据表 (sys_field)
DROP TABLE IF EXISTS sys_field;
CREATE TABLE sys_field (
                           id            VARCHAR(36)  NOT NULL PRIMARY KEY,
                           object_code   VARCHAR(100) NOT NULL,
                           field_name    VARCHAR(100) NOT NULL,
                           field_label   VARCHAR(200),
                           field_type    VARCHAR(50),
                           widget_type   VARCHAR(50),
                           widget_config TEXT,
                           is_required   BOOLEAN DEFAULT FALSE,
                           sort_no       INTEGER DEFAULT 0,

    -- 审计字段
                           tenant_id     VARCHAR(50) DEFAULT '000000',
                           create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           create_by     VARCHAR(64),
                           update_by     VARCHAR(64),
                           del_flag      SMALLINT  DEFAULT 0,
                           version       INTEGER   DEFAULT 1
);

COMMENT ON TABLE sys_field IS '业务字段元数据';
COMMENT ON COLUMN sys_field.id IS '主键ID';
COMMENT ON COLUMN sys_field.object_code IS '所属对象编码';
COMMENT ON COLUMN sys_field.field_name IS '物理字段名';
COMMENT ON COLUMN sys_field.field_label IS '字段显示名';
COMMENT ON COLUMN sys_field.field_type IS '数据类型 (String, Integer, JSON)';
COMMENT ON COLUMN sys_field.widget_type IS 'UI控件类型';
COMMENT ON COLUMN sys_field.widget_config IS '控件配置 (JSON)';
COMMENT ON COLUMN sys_field.is_required IS '是否必填';
COMMENT ON COLUMN sys_field.sort_no IS '排序号';
COMMENT ON COLUMN sys_field.create_time IS '创建时间';
COMMENT ON COLUMN sys_field.update_time IS '更新时间';
COMMENT ON COLUMN sys_field.create_by IS '创建人';
COMMENT ON COLUMN sys_field.update_by IS '更新人';
COMMENT ON COLUMN sys_field.del_flag IS '逻辑删除';
COMMENT ON COLUMN sys_field.version IS '乐观锁版本号';

CREATE INDEX idx_sys_field_obj ON sys_field(object_code);


-- -------------------------------------------------------------------
-- [System Module] 系统管理与认证表 (位于 openx3 模式下)
-- -------------------------------------------------------------------

-- 4. 账号表 (sys_account) - 认证层：自然人属性，全局唯一，仅用于认证
DROP TABLE IF EXISTS sys_account;
CREATE TABLE sys_account (
                             id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                             username    VARCHAR(64) UNIQUE,
                             mobile      VARCHAR(20) UNIQUE,
                             password    VARCHAR(128),
                             salt        VARCHAR(32),
                             status      SMALLINT DEFAULT 1,

    -- 审计字段
                             tenant_id   VARCHAR(50)  DEFAULT '000000',
                             create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                             update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                             create_by   VARCHAR(64),
                             update_by   VARCHAR(64),
                             del_flag    SMALLINT     DEFAULT 0,
                             version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_account IS '账号表（认证层，全局唯一）';
COMMENT ON COLUMN sys_account.id IS '全局唯一ID（sub）';
COMMENT ON COLUMN sys_account.username IS '用户名';
COMMENT ON COLUMN sys_account.mobile IS '手机号';
COMMENT ON COLUMN sys_account.password IS '密码哈希';
COMMENT ON COLUMN sys_account.salt IS '盐';
COMMENT ON COLUMN sys_account.status IS '状态（1=正常 0=禁用）';

CREATE UNIQUE INDEX idx_sys_account_username ON sys_account(username) WHERE del_flag = 0;
CREATE UNIQUE INDEX idx_sys_account_mobile ON sys_account(mobile) WHERE del_flag = 0;


-- 5. 租户表 (sys_tenant) - 业务层：租户隔离
DROP TABLE IF EXISTS sys_tenant;
CREATE TABLE sys_tenant (
                            id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                            name        VARCHAR(200) NOT NULL,

    -- 审计字段
                            tenant_id   VARCHAR(50)  DEFAULT '000000',
                            create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            create_by   VARCHAR(64),
                            update_by   VARCHAR(64),
                            del_flag    SMALLINT     DEFAULT 0,
                            version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_tenant IS '租户表';
COMMENT ON COLUMN sys_tenant.id IS '主键ID';
COMMENT ON COLUMN sys_tenant.name IS '租户名称';


-- 6. 部门表 (sys_dept) - 组织结构（支持树形与继承）
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
                          id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                          tenant_id   VARCHAR(36)  NOT NULL,
                          parent_id   VARCHAR(36)  DEFAULT '0',
                          dept_name   VARCHAR(200) NOT NULL,
                          tree_path   VARCHAR(1000),
                          sort_no     INTEGER      DEFAULT 0,
                          status      SMALLINT     DEFAULT 1,

    -- 审计字段
                          create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          create_by   VARCHAR(64),
                          update_by   VARCHAR(64),
                          del_flag    SMALLINT     DEFAULT 0,
                          version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.tenant_id IS '所属租户';
COMMENT ON COLUMN sys_dept.parent_id IS '父部门ID';
COMMENT ON COLUMN sys_dept.dept_name IS '部门名称';
COMMENT ON COLUMN sys_dept.tree_path IS '树路径（用逗号串联祖先ID，例: dept-root,dept-fin）';
COMMENT ON COLUMN sys_dept.status IS '状态（1=正常 0=禁用）';

CREATE INDEX idx_sys_dept_tenant ON sys_dept(tenant_id);
CREATE INDEX idx_sys_dept_parent ON sys_dept(parent_id);


-- 7. 岗位表 (sys_post) - 组织岗位（租户隔离）
DROP TABLE IF EXISTS sys_post;
CREATE TABLE sys_post (
                          id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                          tenant_id   VARCHAR(36)  NOT NULL,
                          post_code   VARCHAR(64)  NOT NULL,
                          post_name   VARCHAR(200) NOT NULL,
                          status      SMALLINT     DEFAULT 1,

    -- 审计字段
                          create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          create_by   VARCHAR(64),
                          update_by   VARCHAR(64),
                          del_flag    SMALLINT     DEFAULT 0,
                          version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_post IS '岗位表';
COMMENT ON COLUMN sys_post.tenant_id IS '所属租户';
COMMENT ON COLUMN sys_post.post_code IS '岗位编码（如 FINANCE_MGR）';
COMMENT ON COLUMN sys_post.post_name IS '岗位名称';

CREATE UNIQUE INDEX idx_sys_post_code ON sys_post(tenant_id, post_code) WHERE del_flag = 0;


-- 8. 员工表 (sys_employee) - 授权层：业务身份（租户隔离）
DROP TABLE IF EXISTS sys_employee;
CREATE TABLE sys_employee (
                              id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                              account_id  VARCHAR(36),
                              tenant_id   VARCHAR(36)  NOT NULL,
                              dept_id     VARCHAR(36),
                              emp_no      VARCHAR(32),
                              real_name   VARCHAR(64),
                              is_main     BOOLEAN DEFAULT TRUE,
                              is_human    BOOLEAN DEFAULT TRUE,

    -- 审计字段
                              create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                              update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                              create_by   VARCHAR(64),
                              update_by   VARCHAR(64),
                              del_flag    SMALLINT     DEFAULT 0,
                              version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_employee IS '员工表（业务身份，JWT 中 uid）';
COMMENT ON COLUMN sys_employee.account_id IS '关联账号ID（sys_account）';
COMMENT ON COLUMN sys_employee.tenant_id IS '所属租户';
COMMENT ON COLUMN sys_employee.dept_id IS '行政归属部门';
COMMENT ON COLUMN sys_employee.emp_no IS '工号';
COMMENT ON COLUMN sys_employee.real_name IS '姓名/花名';

CREATE INDEX idx_sys_employee_account ON sys_employee(account_id);
CREATE INDEX idx_sys_employee_tenant ON sys_employee(tenant_id);
CREATE INDEX idx_sys_employee_dept ON sys_employee(dept_id);


-- 9. 员工-岗位关联表 (sys_employee_post)
DROP TABLE IF EXISTS sys_employee_post;
CREATE TABLE sys_employee_post (
                                   id          VARCHAR(36) NOT NULL PRIMARY KEY,
                                   employee_id VARCHAR(36) NOT NULL,
                                   post_id     VARCHAR(36) NOT NULL,

    -- 审计字段
                                   tenant_id   VARCHAR(50)  DEFAULT '000000',
                                   create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                   update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                   create_by   VARCHAR(64),
                                   update_by   VARCHAR(64),
                                   del_flag    SMALLINT     DEFAULT 0,
                                   version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_employee_post IS '员工岗位关联表';
CREATE INDEX idx_sep_emp ON sys_employee_post(employee_id);
CREATE INDEX idx_sep_post ON sys_employee_post(post_id);


-- 10. 系统角色表 (sys_role) - 权限容器（含数据权限范围）
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
                          id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                          role_code   VARCHAR(50)  NOT NULL,
                          role_name   VARCHAR(100) NOT NULL,
                          data_scope  VARCHAR(30)  DEFAULT 'ALL',
                          description VARCHAR(500),

    -- 审计字段
                          tenant_id   VARCHAR(50)  DEFAULT '000000',
                          create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          create_by   VARCHAR(64),
                          update_by   VARCHAR(64),
                          del_flag    SMALLINT     DEFAULT 0,
                          version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_role IS '系统角色表';
COMMENT ON COLUMN sys_role.id IS '主键ID';
COMMENT ON COLUMN sys_role.role_code IS '角色编码 (如 admin)';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.data_scope IS '数据权限范围（ALL/DEPT_AND_CHILD/DEPT/SELF/CUSTOM）';
COMMENT ON COLUMN sys_role.description IS '描述';
COMMENT ON COLUMN sys_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_role.create_by IS '创建人';
COMMENT ON COLUMN sys_role.update_by IS '更新人';
COMMENT ON COLUMN sys_role.del_flag IS '逻辑删除';
COMMENT ON COLUMN sys_role.version IS '乐观锁版本号';

CREATE UNIQUE INDEX idx_sys_role_code ON sys_role(role_code) WHERE del_flag = 0;


-- 11. 部门-角色关联表 (sys_dept_role)
DROP TABLE IF EXISTS sys_dept_role;
CREATE TABLE sys_dept_role (
                               id          VARCHAR(36) NOT NULL PRIMARY KEY,
                               dept_id     VARCHAR(36) NOT NULL,
                               role_id     VARCHAR(36) NOT NULL,

    -- 审计字段
                               tenant_id   VARCHAR(50)  DEFAULT '000000',
                               create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               create_by   VARCHAR(64),
                               update_by   VARCHAR(64),
                               del_flag    SMALLINT     DEFAULT 0,
                               version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_dept_role IS '部门角色关联表（用户不得直接绑定角色）';
CREATE INDEX idx_sdr_dept ON sys_dept_role(dept_id);
CREATE INDEX idx_sdr_role ON sys_dept_role(role_id);


-- 12. 岗位-角色关联表 (sys_post_role)
DROP TABLE IF EXISTS sys_post_role;
CREATE TABLE sys_post_role (
                               id          VARCHAR(36) NOT NULL PRIMARY KEY,
                               post_id     VARCHAR(36) NOT NULL,
                               role_id     VARCHAR(36) NOT NULL,

    -- 审计字段
                               tenant_id   VARCHAR(50)  DEFAULT '000000',
                               create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               create_by   VARCHAR(64),
                               update_by   VARCHAR(64),
                               del_flag    SMALLINT     DEFAULT 0,
                               version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_post_role IS '岗位角色关联表（用户通过岗位获得角色）';
CREATE INDEX idx_spr_post ON sys_post_role(post_id);
CREATE INDEX idx_spr_role ON sys_post_role(role_id);


-- 13. 角色-部门关联表 (sys_role_dept)
-- 用于 data_scope = CUSTOM 的场景：指定角色可访问的部门集合
DROP TABLE IF EXISTS sys_role_dept;
CREATE TABLE sys_role_dept (
                               id          VARCHAR(36) NOT NULL PRIMARY KEY,
                               role_id     VARCHAR(36) NOT NULL,
                               dept_id     VARCHAR(36) NOT NULL,

    -- 审计字段
                               tenant_id   VARCHAR(50)  DEFAULT '000000',
                               create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               create_by   VARCHAR(64),
                               update_by   VARCHAR(64),
                               del_flag    SMALLINT     DEFAULT 0,
                               version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_role_dept IS '角色部门关联表（自定义数据权限）';
CREATE INDEX idx_srd_role ON sys_role_dept(role_id);
CREATE INDEX idx_srd_dept ON sys_role_dept(dept_id);


-- 7. 系统菜单表 (sys_menu)
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
                          id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                          parent_id   VARCHAR(36)  DEFAULT '0',
                          title       VARCHAR(100),
                          path        VARCHAR(200),
                          component   VARCHAR(200),
                          perms       VARCHAR(100),
                          icon        VARCHAR(100),
                          type        INTEGER,
                          sort_no     INTEGER      DEFAULT 0,
                          visible     BOOLEAN      DEFAULT TRUE,

    -- 审计字段
                          tenant_id   VARCHAR(50)  DEFAULT '000000',
                          create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          create_by   VARCHAR(64),
                          update_by   VARCHAR(64),
                          del_flag    SMALLINT     DEFAULT 0,
                          version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_menu IS '菜单权限表';
COMMENT ON COLUMN sys_menu.id IS '主键ID';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.title IS '菜单名称';
COMMENT ON COLUMN sys_menu.path IS '路由路径';
COMMENT ON COLUMN sys_menu.component IS '前端组件路径';
COMMENT ON COLUMN sys_menu.perms IS '权限标识 (如 sys:user:add)';
COMMENT ON COLUMN sys_menu.icon IS '图标';
COMMENT ON COLUMN sys_menu.type IS '类型 (0=目录, 1=菜单, 2=按钮)';
COMMENT ON COLUMN sys_menu.sort_no IS '排序号';
COMMENT ON COLUMN sys_menu.visible IS '是否显示';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_menu.create_by IS '创建人';
COMMENT ON COLUMN sys_menu.update_by IS '更新人';
COMMENT ON COLUMN sys_menu.del_flag IS '逻辑删除';
COMMENT ON COLUMN sys_menu.version IS '乐观锁版本号';


-- 8. 角色-菜单关联表 (sys_role_menu)
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
                               id          VARCHAR(36) NOT NULL PRIMARY KEY,
                               role_id     VARCHAR(36) NOT NULL,
                               menu_id     VARCHAR(36) NOT NULL,

    -- 审计字段
                               tenant_id   VARCHAR(50)  DEFAULT '000000',
                               create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               create_by   VARCHAR(64),
                               update_by   VARCHAR(64),
                               del_flag    SMALLINT     DEFAULT 0,
                               version     INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_role_menu IS '角色菜单关联表';
COMMENT ON COLUMN sys_role_menu.id IS '主键ID';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_role_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_role_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_role_menu.create_by IS '创建人';
COMMENT ON COLUMN sys_role_menu.update_by IS '更新人';
COMMENT ON COLUMN sys_role_menu.del_flag IS '逻辑删除';
COMMENT ON COLUMN sys_role_menu.version IS '乐观锁版本号';

CREATE INDEX idx_srm_role ON sys_role_menu(role_id);


-- 9. 权限定义表 (sys_permission)
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
                                id            VARCHAR(36)  NOT NULL PRIMARY KEY,
                                code          VARCHAR(100) NOT NULL,
                                name          VARCHAR(200),
                                object_code   VARCHAR(100),
                                action        VARCHAR(50),
                                description   VARCHAR(500),

    -- 审计字段
                                tenant_id     VARCHAR(50)  DEFAULT '000000',
                                create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                create_by     VARCHAR(64),
                                update_by     VARCHAR(64),
                                del_flag      SMALLINT     DEFAULT 0,
                                version       INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_permission IS '权限定义表（运行时/接口级权限码）';
COMMENT ON COLUMN sys_permission.id IS '主键ID';
COMMENT ON COLUMN sys_permission.code IS '权限码（如 AUTH_SOH_SAVE）';
COMMENT ON COLUMN sys_permission.name IS '权限名称';
COMMENT ON COLUMN sys_permission.object_code IS '对象编码（如 SOH）';
COMMENT ON COLUMN sys_permission.action IS '动作（SAVE/QUERY/DELETE/…）';
COMMENT ON COLUMN sys_permission.description IS '描述';

CREATE UNIQUE INDEX idx_sys_permission_code ON sys_permission(code) WHERE del_flag = 0;


-- 10. 角色-权限关联表 (sys_role_permission)
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
                                     id            VARCHAR(36) NOT NULL PRIMARY KEY,
                                     role_id       VARCHAR(36) NOT NULL,
                                     permission_id VARCHAR(36) NOT NULL,

    -- 审计字段
                                     tenant_id     VARCHAR(50)  DEFAULT '000000',
                                     create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                     update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                     create_by     VARCHAR(64),
                                     update_by     VARCHAR(64),
                                     del_flag      SMALLINT     DEFAULT 0,
                                     version       INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_role_permission IS '角色权限关联表';
COMMENT ON COLUMN sys_role_permission.id IS '主键ID';
COMMENT ON COLUMN sys_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS '权限ID';

CREATE INDEX idx_srp_role ON sys_role_permission(role_id);
CREATE INDEX idx_srp_perm ON sys_role_permission(permission_id);


-- 11. 字段权限策略表 (sys_field_policy) - 列级控制（脱敏/隐藏/加密）
DROP TABLE IF EXISTS sys_field_policy;
CREATE TABLE sys_field_policy (
                                  id            VARCHAR(36) NOT NULL PRIMARY KEY,
                                  role_id       VARCHAR(36) NOT NULL,
                                  resource_code VARCHAR(64) NOT NULL,
                                  field_name    VARCHAR(64) NOT NULL,
                                  policy        VARCHAR(20) NOT NULL,
                                  policy_param  VARCHAR(200),

    -- 审计字段
                                  tenant_id     VARCHAR(50)  DEFAULT '000000',
                                  create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                  update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                  create_by     VARCHAR(64),
                                  update_by     VARCHAR(64),
                                  del_flag      SMALLINT     DEFAULT 0,
                                  version       INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_field_policy IS '字段权限策略表（按角色、资源、字段配置 MASK/HIDDEN/ENCRYPT）';
COMMENT ON COLUMN sys_field_policy.role_id IS '角色ID';
COMMENT ON COLUMN sys_field_policy.resource_code IS '资源编码（如 sys_user_list）';
COMMENT ON COLUMN sys_field_policy.field_name IS '字段名（如 mobile）';
COMMENT ON COLUMN sys_field_policy.policy IS '策略（MASK/HIDDEN/ENCRYPT）';
COMMENT ON COLUMN sys_field_policy.policy_param IS '策略参数（可选）';

CREATE INDEX idx_sfp_role_res ON sys_field_policy(role_id, resource_code);


-- -------------------------------------------------------------------
-- [Integration Module] 第三方系统对接（Client Credentials）
-- -------------------------------------------------------------------

-- 12. 第三方客户端表 (sys_client)
DROP TABLE IF EXISTS sys_client;
CREATE TABLE sys_client (
                            id              VARCHAR(36)  NOT NULL PRIMARY KEY,
                            client_id       VARCHAR(64)  UNIQUE NOT NULL,
                            client_secret   VARCHAR(128) NOT NULL,
                            app_name        VARCHAR(100),
                            ip_whitelist    TEXT,
                            token_validity  INTEGER      DEFAULT 7200,
                            ref_employee_id VARCHAR(36)  UNIQUE,

    -- 审计字段（含 tenant_id）
                            tenant_id       VARCHAR(50)  DEFAULT '000000',
                            create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            create_by       VARCHAR(64),
                            update_by       VARCHAR(64),
                            del_flag        SMALLINT     DEFAULT 0,
                            version         INTEGER      DEFAULT 1
);

COMMENT ON TABLE sys_client IS '第三方客户端（机器账号凭证）';
COMMENT ON COLUMN sys_client.client_id IS 'Client ID（类似账号）';
COMMENT ON COLUMN sys_client.client_secret IS 'Client Secret（类似密码，需加密存储）';
COMMENT ON COLUMN sys_client.ip_whitelist IS 'IP 白名单（逗号分隔，可含 CIDR）';
COMMENT ON COLUMN sys_client.token_validity IS 'Token 有效期（秒）';
COMMENT ON COLUMN sys_client.ref_employee_id IS '关联的虚拟员工ID（Service Account）';


-- -------------------------------------------------------------------
-- [Config Module] 配置/常量（参考 Sage X3：字典、参数、本地菜单）
-- -------------------------------------------------------------------

-- 13. 字典表 (cfg_dict)
DROP TABLE IF EXISTS cfg_dict;
CREATE TABLE cfg_dict (
                          id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                          dict_code   VARCHAR(64)  NOT NULL,
                          dict_name   VARCHAR(200) NOT NULL,
                          status      SMALLINT     DEFAULT 1,
                          remark      VARCHAR(500),

    -- 审计字段（含 tenant_id）
                          tenant_id   VARCHAR(50)  DEFAULT '000000',
                          create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                          create_by   VARCHAR(64),
                          update_by   VARCHAR(64),
                          del_flag    SMALLINT     DEFAULT 0,
                          version     INTEGER      DEFAULT 1
);

CREATE UNIQUE INDEX idx_cfg_dict_code ON cfg_dict(tenant_id, dict_code) WHERE del_flag = 0;


-- 14. 字典项表 (cfg_dict_item)
DROP TABLE IF EXISTS cfg_dict_item;
CREATE TABLE cfg_dict_item (
                               id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                               dict_id     VARCHAR(36)  NOT NULL,
                               item_value  VARCHAR(100) NOT NULL,
                               item_label  VARCHAR(200) NOT NULL,
                               sort_no     INTEGER      DEFAULT 0,
                               status      SMALLINT     DEFAULT 1,

    -- 审计字段（含 tenant_id）
                               tenant_id   VARCHAR(50)  DEFAULT '000000',
                               create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                               create_by   VARCHAR(64),
                               update_by   VARCHAR(64),
                               del_flag    SMALLINT     DEFAULT 0,
                               version     INTEGER      DEFAULT 1
);

CREATE INDEX idx_cfg_dict_item_dict ON cfg_dict_item(dict_id);
CREATE UNIQUE INDEX idx_cfg_dict_item_unique ON cfg_dict_item(dict_id, item_value) WHERE del_flag = 0;


-- 15. 参数表 (cfg_param)
DROP TABLE IF EXISTS cfg_param;
CREATE TABLE cfg_param (
                           id          VARCHAR(36)  NOT NULL PRIMARY KEY,
                           param_code  VARCHAR(64)  NOT NULL,
                           param_name  VARCHAR(200),
                           param_value VARCHAR(500),
                           remark      VARCHAR(500),

    -- 审计字段（含 tenant_id）
                           tenant_id   VARCHAR(50)  DEFAULT '000000',
                           create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                           update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                           create_by   VARCHAR(64),
                           update_by   VARCHAR(64),
                           del_flag    SMALLINT     DEFAULT 0,
                           version     INTEGER      DEFAULT 1
);

CREATE UNIQUE INDEX idx_cfg_param_code ON cfg_param(tenant_id, param_code) WHERE del_flag = 0;


-- 16. 本地菜单 (cfg_local_menu)
DROP TABLE IF EXISTS cfg_local_menu;
CREATE TABLE cfg_local_menu (
                                id            VARCHAR(36)  NOT NULL PRIMARY KEY,
                                parent_id     VARCHAR(36)  DEFAULT '0',
                                title         VARCHAR(200) NOT NULL,
                                function_code VARCHAR(100),
                                url           VARCHAR(500),
                                icon          VARCHAR(100),
                                sort_no       INTEGER      DEFAULT 0,
                                status        SMALLINT     DEFAULT 1,

    -- 审计字段（含 tenant_id）
                                tenant_id     VARCHAR(50)  DEFAULT '000000',
                                create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                create_by     VARCHAR(64),
                                update_by     VARCHAR(64),
                                del_flag      SMALLINT     DEFAULT 0,
                                version       INTEGER      DEFAULT 1
);