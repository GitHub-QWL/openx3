# OpenX3 架构交互图

## 运行时保存流程（Runtime Save）

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant W as Web(OpenX3)
    participant I1 as TokenBlacklistInterceptor
    participant I2 as RuntimePermissionInterceptor
    participant RC as RuntimeController
    participant SE as ScriptEngineService
    participant GD as GenericDao
    participant DB as PostgreSQL
    participant R as Redis
    C->>W: POST /api/runtime/{objectCode}/save
    W->>I1: preHandle(token)
    I1->>R: 检查Token黑名单
    I1-->>W: 通过/拒绝
    W->>I2: preHandle(权限校验)
    I2-->>W: 通过/拒绝
    W->>RC: save(objectCode, data)
    RC->>SE: 执行前置脚本(beforeSave)
    SE-->>RC: 返回处理后的数据
    RC->>GD: insert/update(data)
    GD->>DB: SQL 执行（固定列+JSONB 扩展）
    DB-->>GD: 成功
    GD-->>RC: 成功
    RC-->>W: R.ok(data/id)
    W-->>C: 200 OK
```

相关代码：
- [RuntimeController.java](file:///e:/Code/openx3/backend/openx3-core/src/main/java/com/openx3/core/controller/RuntimeController.java)
- [ScriptEngineService.java](file:///e:/Code/openx3/backend/openx3-core/src/main/java/com/openx3/core/service/ScriptEngineService.java)
- [GenericDao.java](file:///e:/Code/openx3/backend/openx3-core/src/main/java/com/openx3/core/support/GenericDao.java)
- [TokenBlacklistInterceptor.java](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/security/TokenBlacklistInterceptor.java)
- [RuntimePermissionInterceptor.java](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/security/RuntimePermissionInterceptor.java)
- [SecurityWebMvcConfig.java](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/security/SecurityWebMvcConfig.java)

## 运行时查询流程（Runtime Query）

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant W as Web(OpenX3)
    participant I1 as TokenBlacklistInterceptor
    participant I2 as RuntimePermissionInterceptor
    participant RC as RuntimeController
    participant GD as GenericDao
    participant DB as PostgreSQL
    participant R as Redis
    C->>W: GET /api/runtime/{objectCode}/query?filters
    W->>I1: preHandle(token)
    I1->>R: 检查Token黑名单
    I1-->>W: 通过/拒绝
    W->>I2: preHandle(数据范围/字段策略)
    I2-->>W: 通过/拒绝
    W->>RC: query(objectCode, filters)
    RC->>GD: build SQL + 执行
    GD->>DB: SELECT（含数据范围/字段策略）
    DB-->>GD: rows
    GD-->>RC: rows
    RC-->>W: R.ok(page/list)
    W-->>C: 200 OK
```

## 模块交互总览

```mermaid
flowchart LR
    C[Client] --> W[openx3-web]
    subgraph framework[openx3-framework]
      GE[GlobalExceptionHandler]
      SC[SwaggerConfig]
      MC[MybatisPlusConfig]
      RCfg[RedisConfig]
    end
    subgraph system[openx3-system]
      SEC[SecurityWebMvcConfig]
      RBI[TokenBlacklistInterceptor]
      RPI[RuntimePermissionInterceptor]
      AC[AuthController]
    end
    subgraph core[openx3-core]
      RC2[RuntimeController]
      SE2[ScriptEngineService]
      GD2[GenericDao]
    end
    subgraph job[openx3-job]
      QZ[QuartzConfig/JobInitializer]
    end
    DB[(PostgreSQL)]
    REDIS[(Redis)]

    C --> W
    W --> SEC
    SEC --> RBI
    SEC --> RPI
    W --> RC2
    RC2 --> SE2
    RC2 --> GD2
    GD2 --> DB
    AC --> REDIS
    RBI --> REDIS
    QZ --> DB
```

