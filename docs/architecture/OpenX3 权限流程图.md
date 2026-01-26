# OpenX3 权限流程图

## 登录与签发 Token（账号/员工上下文）

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant AC as AuthController
    participant ST as Sa-Token
    participant BL as TokenBlacklistService
    participant R as Redis

    C->>AC: POST /api/auth/login(username,password)
    AC-->>C: 200 OK（返回可选员工列表，不签发Token）

    C->>AC: POST /api/auth/select(accountId,employeeId)
    AC->>ST: 创建登录态（账号+员工上下文）
    ST->>R: 保存会话/权限上下文
    AC-->>C: 200 OK（Authorization: Bearer {jwt}）

    C->>AC: POST /api/auth/switch(employeeId)
    AC->>ST: 切换员工上下文
    ST->>R: 更新会话上下文
    AC-->>C: 200 OK（新Token）
```

相关代码：
- [AuthController.java](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/controller/AuthController.java)
- [StpInterfaceImpl.java](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/security/StpInterfaceImpl.java)
- [TokenBlacklistService.java](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/security/TokenBlacklistService.java)
- [SecurityWebMvcConfig.java](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/security/SecurityWebMvcConfig.java)
- 配置：[application.yml](file:///e:/Code/openx3/backend/openx3-web/src/main/resources/application.yml)（Authorization: Bearer 前缀）

## 请求鉴权与黑名单拦截

```mermaid
flowchart TD
    A[收到请求] --> B{Header: Authorization 是否存在}
    B -- 否 --> Z[匿名/拒绝，视接口安全策略]
    B -- 是 --> C[解析 Bearer Token]
    C --> D[Sa-Token 验证签名与过期]
    D -- 无效 --> Z
    D -- 有效 --> E[TokenBlacklistInterceptor 检查黑名单]
    E -->|在黑名单| Z
    E -->|不在黑名单| F[RuntimePermissionInterceptor 权限校验]
    F -->|通过| G[进入 Controller/Service]
    F -->|拒绝| Z
    G --> H[处理业务并返回]
```

说明：
- 黑名单由 [TokenBlacklistService](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/security/TokenBlacklistService.java) 维护，退出登录时将当前Token加入黑名单
- 权限校验基于用户角色/菜单/权限的匹配与数据范围控制
- 字段级策略由 [FieldPolicyResource/ResponseAdvice](file:///e:/Code/openx3/backend/openx3-system/src/main/java/com/openx3/system/security/field/FieldPolicyResponseAdvice.java) 在响应侧进行脱敏/加密处理

## 退出登录与黑名单

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant AC as AuthController
    participant BL as TokenBlacklistService
    participant R as Redis

    C->>AC: POST /api/auth/logout
    AC->>BL: 将当前Token加入黑名单
    BL->>R: set(token -> blacklist)
    AC-->>C: 200 OK
```

