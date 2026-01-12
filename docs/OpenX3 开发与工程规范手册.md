
# OpenX3 开发与工程规范手册

| 属性       | 详情                                               |
| :------- | :----------------------------------------------- |
| **版本号**  | v1.0.0                                           |
| **更新日期** | 2026-01-06                                       |
| **适用范围** | 后端开发 (Java/Groovy)、数据库设计、API 设计                  |
| **核心架构** | Spring Boot + PostgreSQL (JSONB) + Groovy Engine |

---
## 目录

1. [数据库规范 (PostgreSQL)](#1.数据库规范(postgresql))
	* [1.1 命名约定](#11-命名约定)
	* [1.2 表名前缀定义](#12-表名前缀定义)
	* [1.3 通用字段规范](#13-通用字段规范)
	* [1.4 JSONB 存储规范](#14-jsonb-存储规范)
2. [工程结构与 Maven 模块](#2-工程结构与-maven-模块)
	* [2.1 模块划分](#21-模块划分)
	* [2.2 包名结构](#22-包名结构)
3. [Java 代码开发规范](#3-java-代码开发规范)
	* [3.1 实体类规范](#31-实体类规范)
	* [3.2 异常处理规范](#32-异常处理规范)
	* [3.3 序列化规范](#33-序列化规范)
4. [Groovy 动态脚本规范](#4-groovy-动态脚本规范)
	* [4.1 脚本 ID 命名](#41-脚本-id-命名)
	* [4.2 脚本编写约束](#42-脚本编写约束)
5. [API 接口规范 (RESTful)](#5-api-接口规范-restful)
	* [5.1 路径设计](#51-路径设计)
	* [5.2 统一响应结构](#52-统一响应结构)
6. [Git 提交规范 (Conventional Commits)](#6-git-提交规范-conventional-commits)
7. [附录：核心基础类定义](#7-附录-核心基础类定义)
	* [7.1 BaseEntity.java](#71-baseentityjava)

---

## 1. 数据库规范 (PostgreSQL)

OpenX3 采用 **"固定结构 (Relational) + 扩展结构 (JSONB)"** 的混合存储模式。这是系统的核心基石，请务必严格遵守。

### 1.1 命名约定
所有数据库对象强制使用 **小写蛇形命名法 (snake_case)**。

| 对象 | 命名规则 | 示例 | 说明 |
| :--- | :--- | :--- | :--- |
| **表名** | `{前缀}_{模块}_{描述}` | `sys_user`, `dat_sales_order` | 严禁使用复数 (users ❌)，严禁使用大写 |
| **主键** | `id` | `id` | 统一命名，类型推荐 `VARCHAR(32)` 或 `BIGINT` |
| **外键** | `{关联表简写}_{id}` | `user_id`, `dept_id` | 清晰指明关联关系 |
| **索引** | `idx_{表简写}_{字段}` | `idx_soh_cust_id` | 避免索引名过长 |
| **JSONB列** | `ext_data` | `ext_data` | **强制约定**：动态扩展字段统一存此列 |

### 1.2 表名前缀定义
| 前缀 | 含义 | 用途 |
| :--- | :--- | :--- |
| **`sys_`** | 系统内核 | 存储元数据、用户、权限、菜单 (OpenX3 引擎层) |
| **`dat_`** | 业务数据 | 存储实际业务记录 (如订单、客户、日志) |
| **`cfg_`** | 业务配置 | 存储字典、参数开关 (如审批流配置) |
| **`tmp_`** | 临时数据 | ETL 过程表或临时缓存，允许定期清理 |

### 1.3 通用字段规范 (Standard Fields)
每张业务表（`dat_*`）和核心系统表（`sys_*`）**必须**包含以下 7 个字段：

```sql
id          VARCHAR(32) PRIMARY KEY,     -- 分布式ID (雪花算法)
create_by   VARCHAR(50) NOT NULL,        -- 创建人ID
create_time TIMESTAMP   NOT NULL,        -- 创建时间
update_by   VARCHAR(50),                 -- 更新人ID
update_time TIMESTAMP,                   -- 更新时间
del_flag    SMALLINT    DEFAULT 0,       -- 逻辑删除 (0:正常, 1:删除) ※严禁使用 is_deleted
version     INT         DEFAULT 1,       -- 乐观锁版本号 (每次更新+1)
tenant_id   VARCHAR(50) DEFAULT '000000' -- 租户ID (SaaS预留)

```

### 1.4 JSONB 存储规范 (核心风险点)

**这是最容易出错的地方，必须严格遵守：**

* **物理列名**：使用 **snake_case** (如 `customer_name`)。
* **JSONB 内部 Key**：使用 **camelCase** (如 `vipLevel`)。
* *原因*：JSON 数据经常直接序列化返回给前端 JS，前端习惯驼峰命名。


* **禁止存储**：
* 禁止存二进制数据 (如 Base64 图片)。
* 禁止存超大文本 (1MB+)，应存文件路径。



---

## 2. 工程结构与 Maven 模块

### 2.1 模块划分

```text
openx3-root (Parent pom.xml)
 ├── openx3-api          // [公共层] DTO, Enums, Utils, Exceptions (无 Web 依赖)
 ├── openx3-core         // [内核层] ScriptEngine, GenericDao, MetaService
 ├── openx3-system       // [管理层] User, Role, Menu, Auth Service
 ├── openx3-web          // [启动层] Controller, Filter, Config, Application.main
 └── scripts             // [资源层] Groovy 脚本源码 (开发期本地存放，运行时入库)

```

### 2.2 包名结构 (Package)

根包名：`com.openx3.{module}`

| 层级 | 包名示例 | 约束规范 |
| --- | --- | --- |
| **实体** | `.entity` | 仅对应数据库物理表 (JPA/MyBatis Entity)，**必须继承 BaseEntity** |
| **传输** | `.dto` | 前后端交互对象，**禁止直接暴露 Entity 给前端** |
| **接口** | `.controller` | 仅处理 HTTP 参数解析，**禁止写复杂业务逻辑** |
| **服务** | `.service` | 核心业务逻辑实现 |
| **工具** | `.utils` | 静态工具类 (优先复用 Hutool) |

---

## 3. Java 代码开发规范

### 3.1 实体类规范 (Entity)

所有实体类必须继承 `BaseEntity`，利用 Lombok 简化代码。

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user") // MyBatis-Plus 示例
public class SysUser extends BaseEntity {
    // 物理字段用驼峰，对应数据库 snake_case
    private String userName; 
    
    // JSONB 字段通常不需要定义在这里，由 GenericDao 动态处理
    // 如需强类型映射，可定义为:
    // @TableField(typeHandler = JacksonTypeHandler.class)
    // private Map<String, Object> extData;
}

```

### 3.2 异常处理规范

* **禁止**：捕获异常后吞掉 (`catch (e) {}`) 或仅打印 `e.printStackTrace()`。
* **强制**：业务错误抛出自定义异常 `BusinessException`。
```java
if (order == null) {
    throw new BusinessException(404, "订单不存在"); // 自动被全局拦截器捕获并返回标准 JSON
}

```



### 3.3 序列化规范 (Jackson)

在 `JacksonConfig` 中配置，解决前后端数据格式差异：

* **Long 转 String**：**强制**。防止 JS 精度丢失 (如 ID `123...89` 变 `123...00`)。
* **Date 转 String**：统一格式为 `yyyy-MM-dd HH:mm:ss`。
* **Null 处理**：保留 `null` 字段，不要忽略，确保前端能获取到 Key。

---

## 4. Groovy 动态脚本规范

脚本存储在数据库中，运行时编译。

### 4.1 脚本 ID 命名

| 类别 | 格式 | 示例 | 作用 |
| --- | --- | --- | --- |
| **校验** | `VAL_{Obj}_{Field}` | `VAL_SOH_AMOUNT` | 保存前触发，检查数据合法性 |
| **事件** | `EVT_{Obj}_{Timing}` | `EVT_SOH_POST_SAVE` | 保存后触发，用于回写或通知 |
| **任务** | `JOB_{Name}` | `JOB_SYNC_SAP` | 定时任务调度 |

### 4.2 脚本编写约束

* **禁止 IO**：禁止在脚本中使用 `File`, `Socket` 等 IO 操作（安全沙箱拦截）。
* **禁止 线程**：禁止 `new Thread()`，必须使用平台提供的 `AsyncService`。
* **返回值**：
* **校验脚本**：必须返回 `boolean` (true=通过, false=拦截) 或抛出异常。
* **计算脚本**：必须明确返回类型 (如 `BigDecimal`)，避免返回 String 导致计算错误。



---

## 5. API 接口规范 (RESTful)

### 5.1 路径设计 (kebab-case)

* `GET /api/sales-orders` (列表)
* `GET /api/sales-orders/{id}` (详情)
* `POST /api/sales-orders` (新增)
* `PUT /api/sales-orders/{id}` (修改)
* `DELETE /api/sales-orders/{id}` (删除)

### 5.2 统一响应结构 (Wrapper)

所有 HTTP 接口必须返回 `R<T>` 对象。

```json
{
  "code": 200,            // 业务状态码 (非 HTTP 状态码)
  "success": true,        // 前端通过此字段判断
  "message": "操作成功",
  "data": { ... },        // 实际载荷
  "timestamp": 1709123456789
}

```

---

## 6. Git 提交规范 (Conventional Commits)

每次 Commit 必须遵循以下格式：
`(<scope>): <subject>`

* **type**:
* `feat`: 新功能
* `fix`: 修复 Bug
* `docs`: 文档变更
* `refactor`: 代码重构 (不改变逻辑)
* `chore`: 构建/依赖/杂项


* **示例**:
* `feat(core): 增加 Groovy 脚本热编译缓存机制`
* `fix(user): 修正 del_flag 默认为 null 的问题`



---

## 7. 附录：核心基础类定义

### 7.1 BaseEntity.java

```java
package com.openx3.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {
    // 对应数据库 id
    private String id; 
    
    private String createBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    private String updateBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @JsonIgnore // 前端无需感知，查询时后端自动过滤
    private Integer delFlag; // 0:正常, 1:删除
    
    private Integer version; // 乐观锁
}

```