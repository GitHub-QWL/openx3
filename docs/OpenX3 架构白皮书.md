# OpenX3 架构白皮书
**构建元数据驱动的企业级低代码 PaaS 平台**

| 文档属性     | 详情                                         |
| :------- | :----------------------------------------- |
| **项目名称** | OpenX3 (Open Extensible Enterprise Engine) |
| **版本号**  | v1.0.0 (Planning)                          |
| **更新日期** | 2026-01-06                                 |
| **核心理念** | Java 负责架构，Groovy 负责业务，JSONB 负责数据           |

---

## 目录

1. [执行摘要 (Executive Summary)](#1-执行摘要)
2. [设计哲学与愿景 (Philosophy & Vision)](#2-设计哲学与愿景)
3. [总体技术架构 (Technical Architecture)](#3-总体技术架构)
    * [3.1 技术栈选型与理由](#31-技术栈选型与理由)
    * [3.2 系统分层](#32-系统分层)
    * [3.3 模块划分](#33-模块划分)
4. [核心机制详解 (Core Mechanics)](#4-核心机制详解)
    * [4.1 元数据驱动模型](#41-元数据驱动模型)
    * [4.2 混合存储策略 (Hybrid Storage)](#42-混合存储策略)
    * [4.3 动态脚本引擎 (Script Engine)](#43-动态脚本引擎)
5. [功能与服务规划 (Functional Planning)](#5-功能与服务规划)
6. [权限与安全体系 (Security & RBAC)](#6-权限与安全体系)
7. [部署与运维最佳实践 (Deployment)](#7-部署与运维最佳实践)
8. [从零落地实施路线图 (Implementation Roadmap)](#8-从零落地实施路线图)
9. [附录：核心元数据表结构](#9-附录核心元数据表结构)

---

## 1. 执行摘要

OpenX3 旨在构建一个现代化的、能够替代传统硬编码 ERP 的核心引擎。针对企业软件“需求多变、字段频繁增减、逻辑复杂”的痛点，OpenX3 摒弃了传统的“改代码-编译-发布”流程，采用 **"元数据驱动 (Metadata Driven)"** 模式。

在 OpenX3 中，业务对象、业务逻辑、界面布局、菜单权限皆为**数据**。系统运行时动态解析这些数据，实现**热部署 (Hot-Swap)** 和 **零代码建模 (Zero-Code Modeling)**。

---

## 2. 设计哲学与愿景

* **All is Metadata (一切皆元数据):** 只有核心引擎是硬编码的，所有的业务（销售、采购、库存）都是存储在数据库中的配置。
* **Keep it Simple (保持简单):** 采用“模块化单体”而非过早微服务化，降低运维复杂度。
* **Hybrid Power (混合动力):** * **静态与动态的结合:** Java 提供高性能的基础设施，Groovy 提供灵活的业务逻辑。
    * **关系与文档的结合:** SQL 处理核心关联，JSONB 处理无限扩展字段。

---

## 3. 总体技术架构

### 3.1 技术栈选型与理由

| 组件类别 | 选型 | 版本 | 关键选型理由 |
| :--- | :--- | :--- | :--- |
| **开发语言** | Java | 17+ (LTS) | 生态成熟，性能强劲，类型安全，适合构建复杂的底层引擎。 |
| **应用框架** | Spring Boot | 3.2.x | 行业标准，自动配置，社区支持最好。 |
| **脚本引擎** | **Groovy** | 4.0.x | **核心决策。** 语法兼容 Java，运行于 JVM，支持热编译，性能远超 JS 引擎。 |
| **数据库** | **PostgreSQL** | 15+ | **核心决策。** 其 JSONB 性能优于 MySQL，支持 GIN 索引，是实现混合存储的基石。 |
| **ORM (系统)** | Spring Data JPA | - | 用于管理结构固定的系统表 (`sys_*`)，开发效率高。 |
| **ORM (业务)** | Spring JDBC / MyBatis | - | 用于管理结构动态的业务表，对 SQL 控制力强，适合复杂报表。 |
| **前端框架** | **Baidu Amis** | 3.6+ | **核心决策。** 基于 JSON 配置渲染 UI，完美契合后端元数据驱动的理念。 |
| **中间件** | Redis | 7.x | 分布式缓存、分布式锁、会话管理。 |
| **工具库** | Hutool, Jackson, Lombok | - | 提高开发效率。 |

### 3.2 系统分层

系统采用经典的分层架构，但针对动态性做了特殊设计：

1.  **接入层 (Gateway/Controller):** 统一入口，负责路由分发 (`/api/runtime/{objectCode}/{event}`)。
2.  **服务层 (Platform Services):** 权限校验、事务管理、任务调度。
3.  **内核层 (The Kernel):** * **Meta Engine:** 解析对象定义。
    * **Script Engine:** 编译并执行 Groovy。
    * **Storage Engine:** 处理 JSONB 转换与查询。
4.  **基础设施层 (Infrastructure):** Postgres, Redis, Docker。

### 3.3 模块划分 (Maven Module)

建议工程结构如下：

```text
openx3-root
├── openx3-core          # [核心] 脚本引擎、通用DAO、元数据服务、缓存机制
├── openx3-system        # [管理] 用户、角色、菜单、认证服务
├── openx3-job           # [任务] 基于 Quartz 的定时任务，支持脚本调度
├── openx3-web           # [启动] Controller, Config, Filter, 启动类
└── openx3-api           # [公共] DTO, Utils, Exception, Constants
```
---

## 4. 核心机制详解 (Core Mechanics)

### 4.1 元数据驱动模型 (Metadata Driven Model)
OpenX3 的核心差异在于**“运行时解析”**。系统不包含具体的业务 Java 类（如 `SalesOrder.java`），所有业务对象完全由 `sys_object` 和 `sys_field` 表定义。

* **启动时:** 系统扫描元数据表，加载对象定义到内存缓存。
* **运行时:** `GenericDao` 拦截所有请求，根据元数据定义，动态决定 SQL 的拼装方式。
* **变更时:** 如果用户在界面增加一个“客户等级”字段，后端仅需更新 `sys_field` 表记录，无需修改 Java 代码，无需重启服务器，新字段即刻生效。

### 4.2 混合存储策略 (Hybrid Storage Strategy)
这是解决企业软件“字段无限扩展”痛点的关键架构决策。

* **表结构设计:** 采用“固定列 + 扩展列”模式。
    ```sql
    CREATE TABLE dat_sales_order (
        id VARCHAR(50) PRIMARY KEY,
        status VARCHAR(20),      -- 固定列 (物理字段)
        ext_data JSONB           -- 扩展列 (JSON文档)
    );
    ```
* **写入策略:**
    * **固定字段** (如状态、金额) 写入物理列，保证强类型和极致性能。
    * **动态字段** (如 VIP等级、备注、临时标记) 自动序列化后存入 `ext_data`。
* **查询策略:**
    * **普通查询:** `SELECT * FROM table WHERE status = 'NEW'`
    * **动态查询:** `SELECT * FROM table WHERE ext_data ->> 'vip_level' = 'GOLD'` (利用 PostgreSQL 的 GIN 索引，性能接近 B-Tree)。

### 4.3 动态脚本引擎 (Dynamic Script Engine)
Groovy 是连接静态 Java 和动态业务的桥梁。

* **存储:** 脚本源码（Source Code）以字符串形式存放在 `sys_script` 表中。
* **热加载 (Hot-Swap):** `ScriptEngineService` 后台线程轮询检查数据库时间戳。一旦发现变更，立即使用 `GroovyClassLoader` 重新编译为 Java Class 并替换内存中的旧实例。
* **安全沙箱 (Sandbox):** 在编译阶段通过 `CompilerConfiguration` 实施白名单控制，禁止脚本调用 `System.exit()`, `Runtime.exec()`, 文件 IO 等危险操作。
* **上下文注入 (Context Injection):**
    * `DATA`: 当前业务数据的 Map 对象（双向绑定）。
    * `DB`: 封装好的数据库助手（用于执行 SQL）。
    * `USER`: 当前登录用户上下文。

---

## 5. 功能与服务规划 (Functional Planning)

系统功能模块按照“平台能力”进行划分：

| 模块名称 | 功能点 | 说明 |
| :--- | :--- | :--- |
| **建模引擎** | 对象定义、字段定义、视图定义 | 支持在线配置数据库表结构，自动执行 DDL (Alter Table)。 |
| **UI 引擎** | 布局解析、菜单管理、Amis 适配 | 后端存储 Amis JSON 模板，运行时动态注入字段配置，输出给前端渲染。 |
| **逻辑引擎** | 脚本管理、版本控制、在线调试 | 支持在线编写 Groovy，具备简单的 Web IDE 体验，支持语法高亮和模拟运行。 |
| **流程引擎** | 状态机 (State Machine) | 定义业务数据的流转状态 (如：新建 -> 审核中 -> 已完成)，并绑定脚本事件。 |
| **集成服务** | 导入导出、API 编排 | 支持 Excel 通用导入导出；支持脚本中调用外部 HTTP 接口（如对接微信/SAP）。 |

---

## 6. 权限与安全体系 (Security & RBAC)

### 6.1 RBAC 权限模型
采用标准的 **用户-角色-权限** 模型。
* **功能权限:** 控制菜单的可见性、按钮的可点击性（前端控制显隐，后端拦截器二次校验）。
* **资源标识:** 权限码格式约定为 `AUTH_{Object}_{Action}` (例如 `AUTH_SOH_SAVE`, `AUTH_SOH_AUDIT`)。

### 6.2 数据权限 (Row-Level Security)
这是 ERP 系统的核心难点，必须在底层框架解决。

* **机制:** 采用 AOP 切面或 MyBatis/JDBC 拦截器。
* **规则定义:** 在 `sys_data_rule` 表定义规则 SQL。
    * *示例:* `department_id IN (SELECT dept_id FROM sys_user_dept WHERE user_id = #{currentUserId})`
* **执行:** 在 `GenericDao` 执行查询前，解析当前对象的规则，强制将规则 SQL 拼接到 `WHERE` 子句中，确保用户只能查看到自己权限范围内的数据。

---

## 7. 部署与运维最佳实践 (Deployment)

### 7.1 容器化部署
推荐使用 Docker Compose 或 Kubernetes 进行编排。

**Dockerfile (Backend) 示例:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY target/openx3-web.jar app.jar
# 设置时区为上海时间
ENV TZ=Asia/Shanghai
# 优化 JVM 参数，开启 G1GC 垃圾回收器
ENTRYPOINT ["java", "-XX:+UseG1GC", "-Xmx512m", "-jar", "/app.jar"]
```
### 7.2 目录挂载策略 (Directory Mounting Strategy)
为了防止容器重启导致数据丢失，以及方便开发阶段的脚本调试，建议采用以下挂载策略：

| 宿主机路径 (Host) | 容器路径 (Container) | 用途说明 | 生产环境建议 |
| :--- | :--- | :--- | :--- |
| `./logs` | `/app/logs` | **持久化日志**。防止容器销毁后无法排查历史报错。 | 必须挂载 |
| `./scripts` | `/app/scripts` | **脚本热更新**。开发模式下，本地修改 Groovy 文件，容器内立即生效，无需调 API。 | 仅开发环境 |
| `./config` | `/app/config` | **外部配置**。挂载 `application.yml`，方便修改数据库连接而不重打镜像。 | 推荐挂载 |


---

## 8. 从零落地实施路线图 (Implementation Roadmap)

这是一份给开发团队的详细任务书，请按阶段有序执行，切勿跨越阶段。

### 阶段一：内核基石 (The Nucleus) [预计 2 周]
*目标：跑通“定义-脚本-存储”的最小闭环，不依赖 UI。*

1.  **环境准备**
    * 搭建 Spring Boot 3.2 + Postgres 15 开发环境。
    * 配置 JDBC 连接池 (HikariCP)。
    * 引入 Jackson (JSON处理) 和 Groovy 4.x 依赖。

2.  **元数据层建设**
    * 在数据库建立 `sys_object`, `sys_field`, `sys_script` 表 (SQL参考附录)。
    * 创建对应的 Java 实体类 (Entity) 和 Repository。

3.  **核心引擎开发**
    * **ScriptEngineService:** 实现 Groovy 脚本的动态编译。
        * 功能：监听数据库 `update_time` 变更。
        * 功能：使用 `GroovyClassLoader` 编译代码并缓存到 `ConcurrentHashMap`。
        * 功能：构建 `Binding` 上下文，注入 `DATA` 和 `DB` 变量。
    * **GenericDao:** 实现基于 `sys_field` 的通用存储逻辑。
        * 逻辑：读取 `sys_object` 获取物理表名。
        * 逻辑：读取 `sys_field` 区分物理列和 JSONB 列。
        * 逻辑：组装 SQL (Insert/Update) 并执行。

4.  **MVP 验证**
    * 手动录入一个 `SOH` (销售订单) 的元数据到数据库。
    * 编写一个简单的校验脚本 `SPE_SOH` (如：金额 > 1000 报错)。
    * 通过 Postman 调用 API，验证脚本能否拦截校验，数据能否正确存入 JSONB。

### 阶段二：UI 驱动与建模 (The Modeler) [预计 2 周]
*目标：摆脱数据库工具，实现“零代码”生成界面。*

1.  **Amis 集成**
    * 开发 `/api/meta/window/{code}` 接口。
    * 逻辑：根据请求的 Window Code，从数据库读取 JSON 配置并返回。

2.  **前端壳工程**
    * 搭建一个极简的 HTML 或 React 工程。
    * 实现左侧动态菜单树 (调用 `/api/meta/menu`)。
    * 实现右侧内容区动态加载 Amis 渲染器 (Embed SDK)。

3.  **布局引擎 (Auto-Form)**
    * 开发“表单解析器”服务。
    * 逻辑：读取 `sys_field` 定义。
    * 逻辑：如果字段类型是 `DATE`，自动生成 `{ "type": "input-date" }`。
    * 逻辑：如果字段类型是 `STRING`，自动生成 `{ "type": "input-text" }`。
    * 成果：实现“默认表单”生成，无需手动配置 JSON 也能看到界面。

### 阶段三：安全与服务 (The Guardian) [预计 2 周]
*目标：从“玩具”变成“多用户系统”。*

1.  **身份认证**
    * 集成 JWT 或 Sa-Token。
    * 实现登录接口 `/api/auth/login`，返回 Token。
    * 实现前端 Token 存储与请求头注入 (`Authorization: Bearer ...`)。

2.  **权限拦截**
    * 实现全局拦截器 `SecurityInterceptor`。
    * 逻辑：解析请求 URL 中的 `ObjectCode` (如 SOH)。
    * 逻辑：查询当前用户角色是否拥有 `AUTH_SOH_SAVE` 权限码。

3.  **数据隔离 (Data Scope)**
    * 改造 `GenericDao` 查询方法。
    * 逻辑：实现 SQL 注入过滤器（Data Scope Filter）。
    * 逻辑：在执行 `SELECT` 前，强制拼接 `AND create_by = ?` 或 `AND dept_id = ?`，确保用户只能查询到自己部门的数据。

### 阶段四：开发者生态 (Ecosystem) [持续迭代]
*目标：提升开发体验 (DX)。*

1.  **Web IDE**
    * 在前端集成 Monaco Editor (VS Code 内核)。
    * 实现左侧脚本文件树，右侧代码编辑区。
    * 支持 Ctrl+S 保存时直接调用后端 API 更新 `sys_script` 表，并触发热重载。

2.  **补丁管理**
    * 定义 `.x3p` (OpenX3 Package) 格式 (基于 JSON 或 Zip)。
    * 开发“导出”功能：选择一个业务对象，自动打包其定义、字段、脚本、界面配置。
    * 开发“导入”功能：解析 `.x3p` 包，在目标环境执行 Upsert 操作，实现从开发环境向生产环境的迁移。
	
---

## 9. 附录：核心元数据表结构参考

以下是系统启动必须的 4 张核心表。请在 Postgres 数据库初始化时执行此 SQL 脚本。

这些表构成了 OpenX3 的“元内核”，所有的业务对象（如订单、客户、产品）都将作为数据存储在这些表中。


```sql
-- ==========================================
-- 1. 业务对象定义表 (sys_object)
-- ==========================================
-- 用于注册系统中有哪些业务实体。
-- 例如：code='SOH', name='销售订单', table_name='dat_sales_order'
CREATE TABLE sys_object (
    code VARCHAR(50) PRIMARY KEY,       -- 对象唯一编码 (如: SOH)
    name VARCHAR(100) NOT NULL,         -- 对象显示名称 (如: 销售订单)
    table_name VARCHAR(50) NOT NULL,    -- 对应的物理表名 (如: dat_sales_order)
    pk_field VARCHAR(50) DEFAULT 'id',  -- 物理表的主键字段名
    description TEXT,                   -- 备注说明
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. 字段定义表 (sys_field)
-- ==========================================
-- 定义每个对象的字段结构，决定了数据是存入物理列还是 JSONB。
CREATE TABLE sys_field (
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

-- ==========================================
-- 3. 业务脚本表 (sys_script)
-- ==========================================
-- 存储 Groovy 源代码。ScriptEngineService 会监控此表的变化。
CREATE TABLE sys_script (
    code VARCHAR(50) PRIMARY KEY,       -- 脚本编码 (约定: SPE_{ObjectCode})
    object_code VARCHAR(50),            -- 关联的对象 (可选)
    event VARCHAR(20),                  -- 触发时机: SAVE, DELETE, QUERY, POST_LOAD
    content TEXT NOT NULL,              -- Groovy 源代码字符串
    version INT DEFAULT 1,              -- 版本号 (用于乐观锁或回滚)
    is_active BOOLEAN DEFAULT TRUE,     -- 启用状态
    update_time BIGINT NOT NULL         -- 毫秒级时间戳 (核心：用于热更新检测)
);

-- ==========================================
-- 4. 界面布局表 (sys_window)
-- ==========================================
-- 存储前端界面的配置 (Baidu Amis JSON)。
CREATE TABLE sys_window (
    code VARCHAR(50) PRIMARY KEY,       -- 窗口编码 (如: WIN_SOH_LIST)
    object_code VARCHAR(50),            -- 关联 sys_object.code
    type VARCHAR(20) NOT NULL,          -- 窗口类型: LIST(列表), FORM(表单), MODAL(弹窗)
    layout_json JSONB NOT NULL,         -- Amis 的完整 Schema 配置
    created_by VARCHAR(50),             -- 创建人
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. 数据权限规则表 (sys_data_rule) - (进阶)
-- ==========================================
-- 定义行级数据权限过滤规则。
CREATE TABLE sys_data_rule (
    id SERIAL PRIMARY KEY,
    object_code VARCHAR(50),            -- 作用于哪个对象
    role_code VARCHAR(50),              -- 作用于哪个角色
    rule_type VARCHAR(20),              -- SQL (自定义SQL), DEPT (部门隔离), SELF (仅本人)
    rule_content TEXT,                  -- 规则详情 (如 SQL 片段: "dept_id = {user.deptId}")
    priority INT DEFAULT 0              -- 优先级
);
```

---

**文档结束。**

至此，**OpenX3 架构白皮书** 已全部生成完毕。这份文档已经具备了指导新人从环境搭建到核心引擎开发的全过程。祝你的 OpenX3 项目落地顺利！