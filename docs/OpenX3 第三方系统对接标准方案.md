


# **第三方系统对接标准方案 (M2M Architecture)**

## **1\. 核心设计原则**

1. **机器即用户 (Machine as a User):** 第三方系统在我们的架构中，必须被视为一个“特殊的业务用户”。它必须有身份、有角色、有数据范围，受到同样的拦截器管控。  
2. **凭证分离 (Credential Separation):** 绝不能让第三方系统使用自然人（如管理员）的账号密码登录。必须颁发专属的 `Client ID` 和 `Client Secret`。  
3. **最小权限原则 (Least Privilege):** 第三方系统只能访问其业务必需的接口，绝不能赋予 `admin` 权限。

---

## **2\. 实体模型扩展 (Entity Extension)**

我们需要引入 **应用/客户端 (Application/Client)** 的概念，并将其与现有的“账号-员工”体系打通。

### **2.1 新增表结构：`sys_client`**

用于管理第三方系统的凭证和基础信息。

SQL  
CREATE TABLE sys\_client (  
    id              BIGINT PRIMARY KEY,  
    tenant\_id       BIGINT NOT NULL COMMENT '归属租户',  
    client\_id       VARCHAR(64) UNIQUE NOT NULL COMMENT '类似账号',  
    client\_secret   VARCHAR(128) NOT NULL COMMENT '类似密码(需加密存储)',  
    app\_name        VARCHAR(100) COMMENT '应用名称: ERP同步服务',  
      
    \-- 安全管控  
    ip\_whitelist    TEXT COMMENT 'IP白名单: 192.168.1.0/24',  
    token\_validity  INT DEFAULT 7200 COMMENT 'Token有效期(秒)',  
      
    \-- 核心关联：服务账号  
    ref\_employee\_id BIGINT UNIQUE COMMENT '关联的虚拟员工ID'  
);

### **2.2 核心概念：服务账号 (Service Account)**

这是打通第三方系统与我们现有 RBAC 体系的关键。

* **操作步骤：** 当你创建一个 `SysClient` 时，系统后台自动在 `SysEmployee` 表中创建一个\*\*“虚拟员工”\*\*。  
* **属性设置：**  
  * `real_name`: "ERP Sync Bot"  
  * `dept_id`: 归属到特定的“系统集成部”或根部门。  
  * `is_human`: `false` (标识这是机器)。  
* **权限分配：** 管理员像给普通员工分配权限一样，给这个“虚拟员工”绑定 **角色 (Role)**。  
  * 例如：创建一个角色 `ROLE_ERP_SYNC`，只包含 `order:view` 和 `inventory:update` 权限。

---

## **3\. 对接流程 (The Integration Flow)**

采用标准的 **OAuth 2.0 Client Credentials Grant** 流程。

### **3.1 流程时序图**

代码段  
sequenceDiagram  
    participant 3rd as 第三方系统 (ERP)  
    participant Auth as 认证服务 (Auth Server)  
    participant API as 业务网关 (Gateway)  
      
    Note over 3rd, Auth: 阶段1: 获取凭证  
    3rd-\>\>Auth: POST /oauth/token \<br/\>{grant\_type="client\_credentials", client\_id="...", client\_secret="..."}  
      
    Auth-\>\>Auth: 1\. 校验 ID & Secret\<br/\>2. 校验 IP 白名单\<br/\>3. 查找关联的 Virtual Employee  
    Auth--\>\>3rd: 返回 Access Token (JWT)  
      
    Note over 3rd, API: 阶段2: 业务调用  
    3rd-\>\>API: GET /api/orders/123 \<br/\>Header: Authorization: Bearer {Token}  
      
    API-\>\>API: 1\. 解析 Token\<br/\>2. 提取 uid (虚拟员工)\<br/\>3. 走标准的 RBAC/DataScope 拦截器  
    API--\>\>3rd: 返回数据

### **3.2 Token 载荷设计的微调**

针对第三方系统签发的 Token，Payload 结构与普通用户保持高度一致，以复用后端逻辑：

JSON  
{  
  "sub": "client\_erp\_001",        // Client ID  
  "iss": "iam-system",  
  "auth\_type": "client",          // 标识这是机器登录  
    
  // 关键：复用 bp\_context，骗过后续的业务拦截器  
  "bp\_context": {  
    "uid": "990001",              // 虚拟员工 ID (Service Account)  
    "tid": "tenant\_alibaba",      // 租户ID  
    "dept": "dept\_integration",   // 虚拟部门  
    "posts": \[\]  
  },  
    
  "authorities": \["ROLE\_ERP\_SYNC"\]   
}

---

## **4\. 安全增强策略 (Security Hardening)**

对于第三方对接，密码泄露的风险比自然人更高（因为脚本可能写死在代码里），必须加固：

### **4.1 强制 IP 白名单**

在 `sys_client` 表中配置 `ip_whitelist`。在 `/oauth/token` 接口，**必须**校验请求来源 IP。如果第三方部署在云上，要求其提供 NAT 网关的固定出口 IP。

### **4.2 签名验证 (HMAC Signature) \- 银行级方案**

如果 `client_secret` 泄露，黑客可以在任何 IP（假设没配白名单）调用接口。更高级的做法是要求第三方**对请求参数签名**。

* **规则：** 第三方在调用业务接口时，需在 Header 增加 `X-Signature`。  
* **算法：** `Sign = SHA256(Url + Timestamp + Body + ClientSecret)`。  
* **后端校验：** 网关拦截器取出 Body 和 Header，用同样的算法算一遍。不一致则拒绝。  
* **优点：** 即使 Token 被截获，由于黑客不知道 Secret，无法伪造新的签名（防篡改），且配合 Timestamp 可防重放攻击。

### **4.3 接口限流 (Rate Limiting)**

第三方系统可能会写出死循环 bug，导致针对我方系统的 DoS 攻击。

* **策略：** 针对 `client_id` 设置限流阈值（如：100 QPS）。  
* **实现：** 在网关层 (Nginx/Spring Cloud Gateway) 使用 Redis 令牌桶算法进行限制。

---

## **5\. 架构师的实施建议**

### **场景 A：第三方作为“数据读取者”**

* **配置：** 创建 Client，关联虚拟员工，赋予 `ROLE_READ_ONLY`。  
* **数据权限：** 设置虚拟员工的数据范围为 `ALL` 或 `DEPT`（创建一个专门的部门，把需要共享的数据归属进去）。

### **场景 B：第三方作为“数据写入者”**

* **配置：** 赋予 `ROLE_writer`。  
* **审计：** 这一点至关重要。由于 `SysEmployee` 中记录了 `real_name="ERP Bot"`, 当出现脏数据时，我们在日志表中一查 `created_by = 990001`，立刻就能知道是 ERP 系统同步过来的数据，而不是人工录入的。

---

## **总结**

这种 **"Service Account \+ OAuth2 Client Credentials"** 的模式是目前最成熟的方案。

* **兼容性：** 完美复用了你之前设计的 RBAC、数据权限拦截器、租户隔离逻辑。后端业务代码**不需要写一行** `if (isThirdParty)` 的判断逻辑。  
* **安全性：** 凭证可轮换，权限可控，流量可限。

这是**架构一致性**的完美体现。
