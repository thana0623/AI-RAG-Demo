# 最近 5 条对话与操作（动态窗口）

> 规则：每次新增 1 条，超过 5 条时删除最旧 1 条，仅保留最近 5 条。
> 单条定义：一次对话 + 对应操作（代码/配置/文档/命令）= 1 条。

## Entry-003
- 日期: 2026-04-23
- 清洗后需求: 隐去本地数据库敏感信息，补充 AUTH 的 MySQL 规范，并按天拆分记录。
- 代码变更:
  - .gitignore
  - backend/src/main/resources/application.yml
  - backend/src/main/resources/application-local.yml.example
  - backend/sql/init_mysql.sql
  - docs/AUTH.md
  - .github/prompts/context.md
  - .github/prompts/daily/2026-04-23.md（新增）
- 技术决策:
  - 本地私有配置不入库；公共与示例配置统一使用占位符。
  - 对话记录改为按天归档，降低 context 体积。
- 待办:
  - 提交前检查是否含本地明文配置。

## Entry-004
- 日期: 2026-04-23
- 清洗后需求: 建立递进式 AI 对话日志工作流，支持最近 5 条动态保留与 10 条状态摘要。
- 代码变更:
  - .github/prompts/recent-5.md（新增）
  - .github/prompts/summary-10.md（新增）
  - .github/prompts/workflow-log.md（新增）
  - .github/prompts/context.md（更新）
  - .github/prompts/daily/2026-04-23.md（更新）
- 技术决策:
  - 采用三级记录：daily 原始记录 -> recent-5 清洗窗口 -> summary-10 状态压缩。
- 待办:
  - 补齐第 10 条后执行首轮正式 10 条压缩。

## Entry-005
- 日期: 2026-04-23
- 清洗后需求: Implement PowerShell automation to append one entry, keep only latest 5 in recent log, and update 10-entry stateful summary.
- 代码变更:
  - .github/prompts/scripts/update-dialog-log.ps1
  - .github/prompts/workflow-log.md
- 技术决策:
  - Use script-driven progressive logging workflow.
  - Keep context as index and move details to daily/recent/summary files.
- 待办:
  - When total reaches 10, review auto-generated summary quality.

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
