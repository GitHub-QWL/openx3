-- ==========================================
-- OpenX3 数据库初始化脚本
-- PostgreSQL 15+
-- ==========================================

-- ==========================================
-- 1. 业务对象定义表 (sys_object)
-- ==========================================
-- 用于注册系统中有哪些业务实体。
-- 例如：code='SOH', name='销售订单', table_name='dat_sales_order'
CREATE TABLE IF NOT EXISTS sys_object (
    code VARCHAR(50) PRIMARY KEY,       -- 对象唯一编码 (如: SOH)
    name VARCHAR(100) NOT NULL,         -- 对象显示名称 (如: 销售订单)
    table_name VARCHAR(50) NOT NULL,    -- 对应的物理表名 (如: dat_sales_order)
    pk_field VARCHAR(50) DEFAULT 'id',  -- 物理表的主键字段名
    description TEXT,                   -- 备注说明
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_object IS '业务对象定义表';
COMMENT ON COLUMN sys_object.code IS '对象唯一编码';
COMMENT ON COLUMN sys_object.name IS '对象显示名称';
COMMENT ON COLUMN sys_object.table_name IS '对应的物理表名';
COMMENT ON COLUMN sys_object.pk_field IS '物理表的主键字段名';

-- ==========================================
-- 2. 字段定义表 (sys_field)
-- ==========================================
-- 定义每个对象的字段结构，决定了数据是存入物理列还是 JSONB。
CREATE TABLE IF NOT EXISTS sys_field (
    id SERIAL PRIMARY KEY,
    object_code VARCHAR(50) NOT NULL,   -- 关联 sys_object.code
    field_name VARCHAR(50) NOT NULL,    -- 字段键名 (如: customer_name)
    title VARCHAR(100),                 -- 字段显示标题 (如: 客户名称)
    field_type VARCHAR(20) NOT NULL,    -- 数据类型: STRING, NUMBER, DATE, BOOLEAN, ARRAY
    
    -- 【核心机制开关】
    -- TRUE:  代表该字段在物理表中有对应的列 (高性能，可做外键)
    -- FALSE: 代表该字段将自动序列化存入 ext_data JSONB 列中 (无限扩展)
    is_physical BOOLEAN DEFAULT FALSE,  
    
    is_query BOOLEAN DEFAULT TRUE,      -- 是否允许作为查询条件
    is_required BOOLEAN DEFAULT FALSE,  -- 是否必填 (用于前端验证)
    ui_component VARCHAR(50),           -- 建议的UI组件: input-text, select, date-picker
    
    FOREIGN KEY (object_code) REFERENCES sys_object(code) ON DELETE CASCADE
);

COMMENT ON TABLE sys_field IS '字段定义表';
COMMENT ON COLUMN sys_field.object_code IS '关联 sys_object.code';
COMMENT ON COLUMN sys_field.field_name IS '字段键名';
COMMENT ON COLUMN sys_field.is_physical IS '是否为物理字段 (TRUE:物理列, FALSE:JSONB)';

CREATE INDEX idx_sys_field_object_code ON sys_field(object_code);

-- ==========================================
-- 3. 业务脚本表 (sys_script)
-- ==========================================
-- 存储 Groovy 源代码。ScriptEngineService 会监控此表的变化。
CREATE TABLE IF NOT EXISTS sys_script (
    code VARCHAR(50) PRIMARY KEY,       -- 脚本编码 (约定: SPE_{ObjectCode})
    object_code VARCHAR(50),            -- 关联的对象 (可选)
    event VARCHAR(20),                  -- 触发时机: SAVE, DELETE, QUERY, POST_LOAD
    content TEXT NOT NULL,              -- Groovy 源代码字符串
    version INT DEFAULT 1,              -- 版本号 (用于乐观锁或回滚)
    is_active BOOLEAN DEFAULT TRUE,     -- 启用状态
    update_time BIGINT NOT NULL         -- 毫秒级时间戳 (核心：用于热更新检测)
);

COMMENT ON TABLE sys_script IS '业务脚本表';
COMMENT ON COLUMN sys_script.code IS '脚本编码';
COMMENT ON COLUMN sys_script.content IS 'Groovy 源代码字符串';
COMMENT ON COLUMN sys_script.update_time IS '毫秒级时间戳，用于热更新检测';

CREATE INDEX idx_sys_script_object_code ON sys_script(object_code);
CREATE INDEX idx_sys_script_update_time ON sys_script(update_time);

-- ==========================================
-- 4. 界面布局表 (sys_window)
-- ==========================================
-- 存储前端界面的配置 (Baidu Amis JSON)。
CREATE TABLE IF NOT EXISTS sys_window (
    code VARCHAR(50) PRIMARY KEY,       -- 窗口编码 (如: WIN_SOH_LIST)
    object_code VARCHAR(50),            -- 关联 sys_object.code
    type VARCHAR(20) NOT NULL,          -- 窗口类型: LIST(列表), FORM(表单), MODAL(弹窗)
    layout_json JSONB NOT NULL,         -- Amis 的完整 Schema 配置
    created_by VARCHAR(50),             -- 创建人
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_window IS '界面布局表';
COMMENT ON COLUMN sys_window.code IS '窗口编码';
COMMENT ON COLUMN sys_window.layout_json IS 'Amis 的完整 Schema 配置';

CREATE INDEX idx_sys_window_object_code ON sys_window(object_code);

-- ==========================================
-- 5. 数据权限规则表 (sys_data_rule) - (进阶)
-- ==========================================
-- 定义行级数据权限过滤规则。
CREATE TABLE IF NOT EXISTS sys_data_rule (
    id SERIAL PRIMARY KEY,
    object_code VARCHAR(50),            -- 作用于哪个对象
    role_code VARCHAR(50),              -- 作用于哪个角色
    rule_type VARCHAR(20),              -- SQL (自定义SQL), DEPT (部门隔离), SELF (仅本人)
    rule_content TEXT,                  -- 规则详情 (如 SQL 片段: "dept_id = {user.deptId}")
    priority INT DEFAULT 0              -- 优先级
);

COMMENT ON TABLE sys_data_rule IS '数据权限规则表';
COMMENT ON COLUMN sys_data_rule.object_code IS '作用于哪个对象';
COMMENT ON COLUMN sys_data_rule.rule_content IS '规则详情 (SQL 片段)';

CREATE INDEX idx_sys_data_rule_object_code ON sys_data_rule(object_code);
CREATE INDEX idx_sys_data_rule_role_code ON sys_data_rule(role_code);
