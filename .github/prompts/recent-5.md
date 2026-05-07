# 最近 5 条对话与操作（动态窗口）

> 规则：每次新增 1 条，超过 5 条时删除最旧 1 条，仅保留最近 5 条。
> 单条定义：一次对话 + 对应操作（代码/配置/文档/命令）= 1 条。

## Entry-006
- 日期: 2026-04-28
- 清洗后需求: 按新设计规范重构前端（TypeScript + 自研 UI + 分层结构），同步更新文档与上下文，并将 .idea 排除出版本控制，推进 PR 流程。
- 代码变更:
  - frontend/（结构重建、页面与组件重写、Vite/TS 配置）
  - docs/ARCHITECTURE.md
  - docs/AUTH.md
  - docs/RAG_UPLOAD.md
  - docs/RAG_ASK.md
  - .github/prompts/context.md
  - .github/prompts/daily/2026-04-28.md（新增）
  - .gitignore
- 技术决策:
  - 前端采用 TypeScript + 自研 UI 组件体系，分层调整为 services/store/pages。
  - .idea 不进入 Git，改走分支 PR 流程。
- 待办:
  - 补齐关键流程测试用例并纳入持续验证。

## Entry-007
- 日期: 2026-04-29
- 清洗后需求: 优化 prompts 工程，强制先读 context，再按任务类型读取对应 prompts；需求不清晰时反复追问，明确后再设计和编码。
- 代码变更:
  - .github/copilot-instructions.md
  - .github/prompts/workflow-log.md
  - .github/prompts/backend/backend.md
  - .github/prompts/front/front.md
  - .github/prompts/front/p1.md
  - .github/prompts/context.md
  - .github/prompts/summary-10.md
- 技术决策:
  - 将 prompt 执行顺序固化到项目级指令与场景化 prompt 中。
  - 需求澄清前禁止猜测、禁止设计、禁止写代码。
- 待办:
  - 继续观察后续对话，确认澄清规则是否足够稳定。

## Entry-008
- 日期: 2026-04-29
- 清洗后需求: 为 prompts 工程补充固定追问清单，让需求不清晰时先按统一问题集追问，再进入设计和编码。
- 代码变更:
  - .github/copilot-instructions.md
  - .github/prompts/workflow-log.md
  - .github/prompts/backend/backend.md
  - .github/prompts/front/front.md
  - .github/prompts/front/p1.md
  - .github/prompts/context.md
  - .github/prompts/summary-10.md
- 技术决策:
  - 将澄清问题标准化为固定清单，降低不同任务之间的追问偏差。
  - 继续保持“先问清楚，再设计，再编码”的执行顺序。
- 待办:
  - 观察后续任务中该固定清单是否还能进一步收敛为更短的最小问题集。
## Entry-009
- 日期: 2026-04-29
- 清洗后需求: 实施 prompt 工程第 1 阶段——强化 copilot-instructions.md，加入启动检查、自动路由、执行确认机制。
- 代码变更:
  - .github/copilot-instructions.md（重构执行流程，从建议性改为强制性）
  - .github/prompts/context.md（补充第 1 阶段决策）
  - .github/prompts/summary-10.md
- 技术决策:
  - 智能体每次回答前必须输出启动检查清单、任务类型识别、需求澄清确认、设计评审确认。
  - 引入"✓ Step X 已完成"的执行标记，实现完全可追踪的流程。
- 待办:
  - 观察新执行流程在后续对话中是否生效，需要多轮验证。

## Entry-001
- 日期: 2026-05-07
- 清洗后需求: 按优先级执行项目改进计划 P0 阶段：认证拦截器、BCrypt 密码哈希、Redis 向量存储持久化、Document 实体持久化、输入校验
- 代码变更:
  - 新建 AuthInterceptor/WebMvcConfig/SecurityConfig/Document/DocumentRepository；修改 AuthServiceImpl(RagServiceImpl/AuthController/RagController/VectorStoreConfig/GlobalExceptionHandler/6个Request DTO/pom.xml/init_mysql.sql
- 技术决策:
  - 使用 HandlerInterceptor 而非 Spring Security 全栈；BCrypt 替代 SHA-256 硬编码盐并支持旧密码自动迁移；RedisVectorStore 替代 SimpleVectorStore；Document JPA 实体持久化文档元数据；Jakarta Validation 统一输入校验
- 待办: (无)

