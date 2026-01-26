# OpenX3 —— 元数据驱动的企业级低代码 PaaS

## 项目总览

OpenX3（Open Extensible Enterprise Engine）是以“元数据驱动”为核心的企业级低代码平台。系统仅保留稳定的内核与技术底座，所有业务对象、字段、页面与逻辑以元数据与脚本形式存储并在运行时解析，实现高可用的扩展能力与热更新。

## 技术栈

- Java 21，Gradle 多模块工程
- Spring Boot 3.5.x、Spring Web、Spring Data、MyBatis-Plus
- Sa-Token（JWT/Redis）认证与权限
- SpringDoc OpenAPI（接口文档），Actuator（健康检查）
- PostgreSQL（核心数据）+ JSONB 扩展字段
- Redis（会话与缓存）
- Groovy（动态业务脚本）

## 模块结构

```
backend
├── openx3-common     # 通用常量、异常、工具
├── openx3-framework  # 技术底座（配置、AOP、全局异常、OpenAPI、Redis、MyBatis-Plus）
├── openx3-system     # 系统管理与权限（用户/角色/菜单/字典/租户等）
├── openx3-core       # 内核与运行时（元数据、通用DAO、脚本引擎、运行时控制器）
├── openx3-job        # 定时任务与调度（Quartz）
└── openx3-web        # 应用启动层（入口、资源、应用配置）
```

## 快速开始

### 1. 环境准备

- JDK 21
- PostgreSQL 15+
- Redis 7.x
- Windows/Linux/MacOS 均可

### 2. 初始化数据库

执行 backend/db 目录下脚本：
- backend/db/init.sql
- backend/db/init_data.sql

确保数据库连接与 schema 与脚本保持一致。

### 3. 配置应用

修改应用配置：
- backend/openx3-web/src/main/resources/application.yml
  - 数据库、Redis 连接
  - Sa-Token、JWT 密钥
  - 字段加密密钥

注意：生产环境务必通过环境变量或外部配置注入密钥，避免硬编码在配置文件。

### 4. 启动服务

在 backend 目录执行：

```bash
# Windows
gradlew.bat :openx3-web:bootRun

# Linux/MacOS
./gradlew :openx3-web:bootRun
```

启动类：com.openx3.web.Openx3WebApplication  
端口：8080

### 5. 访问与验证

- OpenAPI 文档： http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON： http://localhost:8080/v3/api-docs
- 健康检查： http://localhost:8080/actuator/health

认证使用 Bearer Token：
- Header: Authorization: Bearer {token}

## 关键能力

- 元数据驱动：业务对象与字段定义存储在数据库，运行时解析
- 混合存储：固定列保证强类型与性能，JSONB 扩展字段提供无限扩展
- 动态脚本：Groovy 脚本热加载，支持注入运行时上下文
- 权限体系：Sa-Token + 拦截器实现登录态、黑名单与细粒度权限控制

## 目录导航

- 后端根构建与设置：[backend/build.gradle](backend/build.gradle)，[backend/settings.gradle](backend/settings.gradle)
- 应用配置：[application.yml](backend/openx3-web/src/main/resources/application.yml)
- 启动入口：[Openx3WebApplication.java](backend/openx3-web/src/main/java/com/openx3/web/Openx3WebApplication.java)
- 全局异常与配置：
  - [GlobalExceptionHandler.java](backend/openx3-framework/src/main/java/com/openx3/framework/web/GlobalExceptionHandler.java)
  - [SwaggerConfig.java](backend/openx3-framework/src/main/java/com/openx3/framework/config/SwaggerConfig.java)
  - [MybatisPlusConfig.java](backend/openx3-framework/src/main/java/com/openx3/framework/config/MybatisPlusConfig.java)
- 元数据与运行时：
  - [RuntimeController.java](backend/openx3-core/src/main/java/com/openx3/core/controller/RuntimeController.java)
  - [ScriptEngineService.java](backend/openx3-core/src/main/java/com/openx3/core/service/ScriptEngineService.java)
- 系统管理与权限：
  - [AuthController.java](backend/openx3-system/src/main/java/com/openx3/system/controller/AuthController.java)
  - [SecurityWebMvcConfig.java](backend/openx3-system/src/main/java/com/openx3/system/security/SecurityWebMvcConfig.java)

## 深入阅读

- docs/OpenX3 架构白皮书.md
- docs/OpenX3 权限设计.md
- docs/OpenX3 开发与工程规范手册.md
- docs/OpenX3 第三方系统对接标准方案.md
 - 架构交互图：docs/architecture/OpenX3 架构交互图.md
 - 权限流程图：docs/architecture/OpenX3 权限流程图.md

## 许可与贡献

- 变更记录：CHANGELOG.md
- 贡献指南：CONTRIBUTING.md
