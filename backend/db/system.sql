-- ==========================================
-- OpenX3 系统表初始化脚本
-- 用户、角色、权限、菜单等系统核心表
-- ==========================================

-- ==========================================
-- 1. 用户表 (sys_user)
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_user (
    id VARCHAR(32) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status SMALLINT DEFAULT 1,          -- 状态: 0-禁用, 1-启用
    dept_id VARCHAR(32),                -- 部门ID
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    version INT DEFAULT 1,
    tenant_id VARCHAR(50) DEFAULT '000000'
);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.status IS '状态: 0-禁用, 1-启用';

CREATE INDEX idx_sys_user_username ON sys_user(username);
CREATE INDEX idx_sys_user_dept_id ON sys_user(dept_id);

-- ==========================================
-- 2. 角色表 (sys_role)
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_role (
    id VARCHAR(32) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status SMALLINT DEFAULT 1,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    version INT DEFAULT 1,
    tenant_id VARCHAR(50) DEFAULT '000000'
);

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.code IS '角色编码';

CREATE INDEX idx_sys_role_code ON sys_role(code);

-- ==========================================
-- 3. 用户角色关联表 (sys_user_role)
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    role_id VARCHAR(32) NOT NULL,
    UNIQUE(user_id, role_id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);

-- ==========================================
-- 4. 菜单表 (sys_menu)
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_menu (
    id VARCHAR(32) PRIMARY KEY,
    parent_id VARCHAR(32),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    path VARCHAR(255),
    component VARCHAR(255),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    version INT DEFAULT 1,
    tenant_id VARCHAR(50) DEFAULT '000000'
);

COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.code IS '菜单编码';

CREATE INDEX idx_sys_menu_parent_id ON sys_menu(parent_id);
CREATE INDEX idx_sys_menu_code ON sys_menu(code);

-- ==========================================
-- 5. 权限表 (sys_permission)
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_permission (
    id VARCHAR(32) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,   -- 权限码 (如: AUTH_SOH_SAVE)
    name VARCHAR(100) NOT NULL,
    object_code VARCHAR(50),             -- 关联的业务对象
    action VARCHAR(50),                  -- 操作类型: SAVE, DELETE, QUERY, AUDIT
    description TEXT,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    version INT DEFAULT 1,
    tenant_id VARCHAR(50) DEFAULT '000000'
);

COMMENT ON TABLE sys_permission IS '权限表';
COMMENT ON COLUMN sys_permission.code IS '权限码 (如: AUTH_SOH_SAVE)';
COMMENT ON COLUMN sys_permission.object_code IS '关联的业务对象';

CREATE INDEX idx_sys_permission_code ON sys_permission(code);
CREATE INDEX idx_sys_permission_object_code ON sys_permission(object_code);

-- ==========================================
-- 6. 角色权限关联表 (sys_role_permission)
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id SERIAL PRIMARY KEY,
    role_id VARCHAR(32) NOT NULL,
    permission_id VARCHAR(32) NOT NULL,
    UNIQUE(role_id, permission_id)
);

COMMENT ON TABLE sys_role_permission IS '角色权限关联表';

CREATE INDEX idx_sys_role_permission_role_id ON sys_role_permission(role_id);
CREATE INDEX idx_sys_role_permission_permission_id ON sys_role_permission(permission_id);

-- ==========================================
-- 7. 部门表 (sys_dept)
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_dept (
    id VARCHAR(32) PRIMARY KEY,
    parent_id VARCHAR(32),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    version INT DEFAULT 1,
    tenant_id VARCHAR(50) DEFAULT '000000'
);

COMMENT ON TABLE sys_dept IS '部门表';

CREATE INDEX idx_sys_dept_parent_id ON sys_dept(parent_id);
CREATE INDEX idx_sys_dept_code ON sys_dept(code);
