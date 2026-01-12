-- ==========================================
-- OpenX3 定时任务表初始化脚本
-- ==========================================

-- ==========================================
-- 定时任务表 (sys_job)
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_job (
    id VARCHAR(32) PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    job_group VARCHAR(50),
    job_type VARCHAR(20) NOT NULL,        -- SCRIPT: Groovy脚本, BEAN: Spring Bean方法
    script_code VARCHAR(50),              -- 脚本编码（当 jobType=SCRIPT 时使用）
    bean_name VARCHAR(100),                -- Bean名称（当 jobType=BEAN 时使用）
    method_name VARCHAR(100),              -- 方法名（当 jobType=BEAN 时使用）
    cron_expression VARCHAR(100) NOT NULL, -- Cron 表达式
    status SMALLINT DEFAULT 1,             -- 0-暂停, 1-运行
    description TEXT,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    version INT DEFAULT 1,
    tenant_id VARCHAR(50) DEFAULT '000000'
);

COMMENT ON TABLE sys_job IS '定时任务表';
COMMENT ON COLUMN sys_job.job_type IS '任务类型: SCRIPT-脚本任务, BEAN-Bean方法任务';
COMMENT ON COLUMN sys_job.cron_expression IS 'Cron 表达式';

CREATE INDEX idx_sys_job_status ON sys_job(status);
CREATE INDEX idx_sys_job_group ON sys_job(job_group);
