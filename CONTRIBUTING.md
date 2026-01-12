## 核心贡献规范（强制）
### 1. 分支规范
main/master: 生产分支，禁止直接提交；develop: 开发基准分支
feature/xxx: 功能开发 | fix/xxx: BUG修复 | hotfix/xxx: 紧急线上修复

### 2. Commit规范
feat: 新增功能 | fix: 修复BUG | docs: 文档修改 | style: 格式调整 | refactor: 代码重构 | test: 测试用例 | build: 构建配置 | chore: 杂项修改
示例：feat: 用户模块新增短信登录，fix: 修复前端分页数据异常

### 3. 提交流程
1. 基于develop拉取最新代码 → 建子分支开发 → 本地自测
2. 推送分支 → 提MR/PR至develop → 代码审核 → 合并
3. develop测试通过 → 由负责人合并至main/master → 打版本标签

### 4. 代码要求
- 语义化命名，必要注释，无硬编码，完善异常处理
- 提交前自查：无语法错误、无冗余代码、无无关文件
- 文档与代码同步更新，接口文档统一存放docs/tech/

### 5. 底线
禁止直接向主分支提交代码，禁止无规范的commit，禁止提交无法运行的代码。