# OpenX3 - 元数据驱动的企业级低代码 PaaS 平台

## 项目简介

OpenX3 (Open Extensible Enterprise Engine) 是一个现代化的、能够替代传统硬编码 ERP 的核心引擎。针对企业软件"需求多变、字段频繁增减、逻辑复杂"的痛点，OpenX3 采用 **"元数据驱动 (Metadata Driven)"** 模式，实现**热部署 (Hot-Swap)** 和 **零代码建模 (Zero-Code Modeling)**。

## 核心理念

- **All is Metadata (一切皆元数据):** 只有核心引擎是硬编码的，所有的业务（销售、采购、库存）都是存储在数据库中的配置。
- **Keep it Simple (保持简单):** 采用"模块化单体"而非过早微服务化，降低运维复杂度。
- **Hybrid Power (混合动力):** 
  - 静态与动态的结合：Java 提供高性能的基础设施，Groovy 提供灵活的业务逻辑。
  - 关系与文档的结合：SQL 处理核心关联，JSONB 处理无限扩展字段。

## 技术栈

| 组件类别 | 选型 | 版本 |
|---------|------|------|
| 开发语言 | Java | 17+ (LTS) |
| 应用框架 | Spring Boot | 3.2.x |
| 脚本引擎 | Groovy | 4.0.x |
| 数据库 | PostgreSQL | 15+ |
| ORM | Spring Data JPA / MyBatis Plus | - |
| 前端框架 | Baidu Amis | 3.6+ |
| 中间件 | Redis | 7.x |

## 模块结构

```
openx3-root
├── openx3-api          # [公共层] DTO, Enums, Utils, Exceptions
├── openx3-core         # [内核层] ScriptEngine, GenericDao, MetaService
├── openx3-system       # [管理层] User, Role, Menu, Auth Service
└── openx3-web          # [启动层] Controller, Config, Filter, Application.main
```

## 快速开始

### 1. 环境要求

- JDK 17+
- PostgreSQL 15+
- Redis 7.x
- Gradle 7.x+

### 2. 数据库初始化

执行以下 SQL 脚本初始化数据库：

- `src/main/resources/db/schema.sql` - 元数据表结构
- `src/main/resources/db/system.sql` - 系统表结构

### 3. 配置修改

修改 `openx3-web/src/main/resources/application.yml` 中的数据库和 Redis 连接信息。

### 4. 启动项目

```bash
# 使用 Gradle 启动
cd openx3-web
../gradlew bootRun

# 或使用 IDE 直接运行
# 启动类: com.openx3.web.Openx3WebApplication
```

### 5. 访问接口

- API 文档: http://localhost:8080/api
- 健康检查: http://localhost:8080/actuator/health

## 核心功能

### 1. 元数据驱动模型

所有业务对象完全由 `sys_object` 和 `sys_field` 表定义，系统运行时动态解析这些数据。

### 2. 混合存储策略

采用"固定列 + 扩展列"模式：
- **固定字段** (如状态、金额) 写入物理列，保证强类型和极致性能。
- **动态字段** (如 VIP等级、备注) 自动序列化后存入 `ext_data` JSONB 列。

### 3. 动态脚本引擎

- Groovy 脚本存储在数据库中，支持热加载。
- 脚本执行时注入上下文变量：`DATA`、`DB`、`USER` 等。

## API 接口

### 认证接口

- `POST /api/auth/login` - 用户登录
- `GET /api/auth/current` - 获取当前用户信息

### 运行时接口

- `POST /api/runtime/{objectCode}/save` - 保存数据
- `PUT /api/runtime/{objectCode}/update/{id}` - 更新数据
- `GET /api/runtime/{objectCode}/query` - 查询数据

### 元数据接口

- `GET /api/meta/object/{code}` - 获取业务对象定义
- `GET /api/meta/fields/{objectCode}` - 获取字段定义

## 开发规范

请参考 `OpenX3 开发与工程规范手册.md` 了解详细的开发规范。

## 许可证

[待定]

## 联系方式

[待定]
