# **企业级统一身份认证与授权平台 (IAM) 架构设计书**

版本： v3.0 (Master Design)

设计目标： 成熟 (Mature)、严谨 (Rigorous)、灵活 (Flexible)

适用场景： 集团型企业、SaaS 平台、复杂 B2B 系统

---

## **1\. 核心架构哲学 (Architecture Philosophy)**

本系统的设计建立在以下**四大铁律**之上，开发过程中**严禁违反**：

1. **双层身份隔离 (Dual-Layer Identity):**  
   * **账号 (Account):** 自然人属性，全局唯一，仅用于认证 (Authentication)。  
   * **员工 (Employee):** 业务属性，租户隔离，仅用于授权 (Authorization)。  
2. **角色承载权限 (Role-Based Only):**  
   * 严禁将权限（如 order:delete）直接授予用户。  
   * 用户通过 **岗位 (Post)** 或 **部门 (Dept)** 间接获取角色。  
3. **立体化管控 (3D Control):**  
   * 权限必须覆盖三个维度：**功能** (能点什么)、**数据** (能看多少)、**字段** (能看多细)。  
4. **无状态与上下文 (Stateless Context):**  
   * 使用 JWT 携带业务上下文 (Context)，服务端不存储 Session 状态，但通过 Redis 黑名单实现可控的强制注销。

---

## **2\. 核心实体模型 (Domain Model & ERD)**

我们采用 **Account-Employee-Post-Role** 复合模型，解决“一人多职”、“部门主管权限”、“SaaS 多租户”等复杂问题。

### **2.1 实体关系图**

代码段  
erDiagram  
    %% Layer 1: 认证层 (全局唯一)  
    SysAccount {  
        long id PK "全局唯一ID (sub)"  
        string mobile "登录账号"  
        string password\_hash  
        int status "禁用/锁定"  
    }

    %% Layer 2: 业务层 (租户隔离)  
    SysTenant {  
        long id PK  
        string name "租户/分公司名称"  
    }

    SysEmployee {  
        long id PK "业务ID (uid)"  
        long account\_id FK  
        long tenant\_id FK  
        long dept\_id FK "行政归属"  
        boolean is\_main "是否主职"  
    }

    %% 组织与岗位  
    SysDept {  
        long id PK  
        string tree\_path "1,10,100"  
    }  
      
    SysPost {  
        long id PK  
        string code "e.g. FINANCE\_MGR"  
        string name "岗位: 财务经理"  
    }

    %% 权限核心  
    SysRole {  
        long id PK  
        string code "ROLE\_APPROVER"  
        enum data\_scope "ALL, DEPT, SELF..."  
    }

    %% 关系链  
    SysAccount ||--o{ SysEmployee : "1:N (多租户/多职)"  
    SysTenant ||--o{ SysEmployee : "contains"  
      
    SysEmployee }|--|| SysDept : "belongs\_to (继承基础权限)"  
    SysEmployee ||--o{ SysUserPost : "holds (获取岗位权限)"  
      
    SysDept ||--o{ SysDeptRole : "binds"  
    SysPost ||--o{ SysPostRole : "binds"  
      
    SysRole ||--o{ SysMenu : "controls\_func"  
    SysRole ||--o{ SysFieldPolicy : "controls\_field"

### **2.2 关键表结构设计**

SQL  
\-- 1\. 账号表 (自然人)  
CREATE TABLE sys\_account (  
    id          BIGINT PRIMARY KEY COMMENT '全局唯一ID',  
    username    VARCHAR(64) UNIQUE,  
    mobile      VARCHAR(20) UNIQUE,  
    password    VARCHAR(128),  
    salt        VARCHAR(32),  
    status      TINYINT DEFAULT 1 COMMENT '1:正常 0:禁用'  
);

\-- 2\. 员工/业务用户表 (业务身份)  
CREATE TABLE sys\_employee (  
    id          BIGINT PRIMARY KEY COMMENT '业务ID, JWT中的uid',  
    account\_id  BIGINT NOT NULL COMMENT '关联sys\_account',  
    tenant\_id   BIGINT NOT NULL COMMENT '所属租户',  
    dept\_id     BIGINT COMMENT '行政归属部门',  
    emp\_no      VARCHAR(32) COMMENT '工号',  
    real\_name   VARCHAR(64) COMMENT '业务花名'  
);

\-- 3\. 岗位表 (解决主管/兼职问题)  
CREATE TABLE sys\_post (  
    id          BIGINT PRIMARY KEY,  
    tenant\_id   BIGINT,  
    post\_code   VARCHAR(64) COMMENT '编码: FINANCE\_MGR',  
    post\_name   VARCHAR(64) COMMENT '名称: 财务经理'  
);

\-- 4\. 角色表 (权限容器)  
CREATE TABLE sys\_role (  
    id          BIGINT PRIMARY KEY,  
    role\_code   VARCHAR(64) UNIQUE,  
    role\_name   VARCHAR(64),  
    data\_scope  CHAR(1) DEFAULT '1' COMMENT '1:全部 2:本部门及子 3:本部门 4:本人 5:自定义',  
    remark      VARCHAR(255)  
);

\-- 5\. 字段权限策略表 (列级控制)  
CREATE TABLE sys\_field\_policy (  
    id            BIGINT PRIMARY KEY,  
    role\_id       BIGINT,  
    resource\_code VARCHAR(64) COMMENT '资源: sys\_user\_list',  
    field\_name    VARCHAR(64) COMMENT '字段: mobile',  
    policy        VARCHAR(20) COMMENT '策略: MASK(掩码), HIDDEN(隐藏), ENCRYPT(加密)'  
);

---

## **3\. 认证与令牌设计 (AuthN & Token)**

采用 **OAuth2 / OIDC** 标准，结合 **JWT \+ Redis (Hybrid)** 模式。

### **3.1 Token 载荷 (Payload)**

Token 必须严格区分“我是谁”和“我在哪”。

JSON  
{  
  "sub": "10086",                 // Account ID (自然人，用于审计)  
  "exp": 1735689600,  
  "jti": "uuid-v4",               // 用于黑名单校验  
    
  // 业务上下文 (核心)  
  "bp\_context": {  
    "uid": "2005001",             // Employee ID (业务主键，用于DB记录)  
    "tid": "tenant\_alibaba",      // 租户隔离ID (用于SQL拦截器)  
    "dept": "dept\_finance",       // 部门ID  
    "posts": \["FINANCE\_MGR"\]      // 当前岗位列表  
  },  
    
  // 权限快照 (仅放关键信息，避免Header过大)  
  "authorities": \["ROLE\_MGR", "ROLE\_USER"\],   
  "ds\_scope": "DEPT\_AND\_CHILD"    // 当前最大数据权限范围  
}

### **3.2 登录与切换流程**

1. **Login:** 用户输入手机号+密码 $\\rightarrow$ 验证 SysAccount $\\rightarrow$ 返回该账号下的所有 SysEmployee 列表。  
2. **Select Context:** 用户选择“进入 子公司A” $\\rightarrow$ 服务端签发包含 tid=sub\_comp\_a 的 Access Token。  
3. **Switch:** 用户切换身份 $\\rightarrow$ 携带旧 Token 请求 /auth/switch $\\rightarrow$ 验证通过后签发新 Token。

---

## **4\. 三维授权机制 (3D AuthZ)**

### **4.1 维度一：功能权限 (Functional)**

* **定义：** 菜单可见性、按钮可点击性、API 可访问性。  
* **计算逻辑：** $P\_{final} \= P(Role\_{dept}) \\cup P(Role\_{post}) \\cup P(Role\_{direct})$  
* **实现：**  
  * 前端：v-auth="\['user:add'\]" 指令控制 DOM。  
  * 后端：Spring Security @PreAuthorize("hasAuthority('user:add')")。

### **4.2 维度二：数据权限 (Data Scope)**

* **定义：** SQL 查询时的行级过滤 (Row-Level Security)。  
* **实现机制：AOP \+ MyBatis Interceptor**  
  * **开发规范：** 业务代码**不写**权限 WHERE 条件。Mapper 方法标记 @DataScope(alias="t")。  
  * **拦截逻辑：**  
    1. 拦截器解析 Token 中的 ds\_scope。  
    2. **CASE ALL:** 不追加条件。  
    3. **CASE DEPT:** 自动追加 AND t.dept\_id \= {token.dept\_id}。  
    4. **CASE SELF:** 自动追加 AND t.create\_by \= {token.uid}。  
  * **严谨性：** 即使开发人员忘记写权限代码，注解机制也能兜底保证数据不泄露。

### **4.3 维度三：字段权限 (Field Visibility)**

* **定义：** JSON 返回时的列级脱敏。  
* **实现机制：JSON Serializer Interceptor**  
  * 流程： 1\. Controller 返回 User 对象。  
    2\. Jackson/Gson 序列化器触发。  
    3\. 检查缓存中的 sys\_field\_policy。  
    4\. 如果当前角色对 mobile 字段策略为 MASK，则将 "13800138000" 覆写为 "138\*\*\*\*8000"。  
  * **优点：** 数据在离开服务器的最后一刻被截断，绝对安全。

---

## **5\. 个性化与配置 (Configuration)**

### **5.1 动态主页 (Dashboard)**

支持 **Config Merge Strategy**：

* **Structure:** { layout: "grid", widgets: \[...\] }  
* **Priority:** User Config (用户拖拽) \> Role Config (岗位默认) \> System Default。

### **5.2 安全策略基线 (Security Baseline)**

由管理员配置，全局生效：

* **密码策略：** 必须包含 (大写+小写+数字+符号)，长度 \> 10，90天强制更换。  
* **防爆破：** 同一 IP/账号 1分钟错误 5 次，锁定 30 分钟。  
* **审计：** 记录所有写操作 (POST/PUT/DELETE)，包含 AccountID, EmployeeID, IP, OldValue, NewValue。

---

## **6\. 实施路线图 (Implementation Roadmap)**

### **Phase 1: 骨架构建 (The Skeleton)**

* 实现 SysAccount, SysEmployee, SysTenant 表结构。  
* 搭建 OAuth2 认证服务，实现双层登录流程。  
* 完成 JWT Token 的签发与网关解析。

### **Phase 2: 核心 RBAC (The Body)**

* 实现 SysPost, SysRole, SysMenu。  
* 完成“岗位关联角色”、“部门关联角色”的逻辑开发。  
* 实现前端菜单动态渲染。

### **Phase 3: 高级管控 (The Shield)**

* **开发数据权限拦截器 (DataScope Interceptor)。** (这是技术难点)  
* **开发字段脱敏序列化器 (Field Masking Serializer)。**  
* 接入 Redis 实现 Token 黑名单与强制注销。

---

## **7\. 架构师总结**

本方案通过 **“账号-用户分离”** 保证了系统的灵活性（支持 SaaS），通过 **“岗位模型”** 解决了企业复杂的行政与业务授权矛盾，通过 **“拦截器切面”** 保证了数据与字段权限的严谨落地。这是一个经得起安全审计和业务扩展考验的成熟架构。

