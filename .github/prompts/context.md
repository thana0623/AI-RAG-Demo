# 项目上下文总览（Context）

> 用途：统一沉淀项目当前技术栈、历史决策、待办事项，以及每日记录索引。
> 
> 维护约定：每次功能开发/修复后，除更新对应 `docs/*.md` 外，也要同步更新本文档；每次 Git 提交建议包含本文档变更。

## 1. 当前技术栈

### 后端
- Java 17
- Spring Boot 3.2.4
- Spring AI（Embedding / Chat / VectorStore）
- Spring Data JPA
- Maven

### 前端
- Vue 3 + Vite + TypeScript
- 自研基础 UI 组件（BaseButton / BaseInput / BaseCard 等）
- Pinia（状态管理）
- Vue Router 4（路由管理）
- Axios（统一 API 封装层）

### 中间件与存储
- RabbitMQ（异步向量化任务队列）
- Redis（Token、验证码、问答缓存、任务状态）
- MySQL 8.x（当前用户体系持久层，默认库：rag_demo）

### 配置与环境
- 公共配置：backend/src/main/resources/application.yml（可提交）
- 本地覆盖：backend/src/main/resources/application-local.yml（本地私有，不提交）
- 本地模板：backend/src/main/resources/application-local.yml.example（可提交）

## 2. 历史决策记录

### 架构与工程决策
1. 采用经典 MVC 分层：controller / service / repository / model / config / mq。
2. 控制器统一通过 Result<T> 返回，前后端约定 code/data/message 响应结构。
3. 文档向量化使用 RabbitMQ 异步化，避免同步阻塞主请求。
4. Redis 同时承担认证态与 RAG 业务缓存职责：
   - auth token（7天）
   - 邮箱验证码（10分钟）
   - qa_cache（10分钟）
   - 文档向量化状态（PENDING/PROCESSING/SUCCESS/FAILED）
5. 配置管理采用三层文件：application.yml + application-local.yml + application-local.yml.example，避免敏感信息入库。
6. 关系数据库已由 H2 切换为本地 MySQL，并提供初始化脚本 backend/sql/init_mysql.sql。
7. 前端采用分层架构：services/（请求层）→ store/（状态层）→ pages/（视图层），API 调用统一从 services/ 层导入。
8. 前端去除 Arco Design，使用自研基础 UI 组件；Pinia 管理全局状态，Vue Router 4 管理路由及导航守卫。

### 认证模块决策（AUTH）
1. 注册、登录、发送验证码、重置密码、获取当前用户信息完整闭环。
2. 登录支持用户名/邮箱，返回 Bearer Token。
3. 密码存储采用 SHA-256 + Salt。
4. 验证码按业务类型区分 REGISTER / RESET。

### RAG 问答决策（RAG_ASK）
1. 先查 Redis 缓存（qa_cache:<question>），命中即返回。
2. 未命中则走向量检索（Cosine + Top-K）并拼接上下文到 Prompt。
3. 大模型回答后写回缓存并设置过期时间（10分钟）。

### 文档上传向量化决策（RAG_UPLOAD）
1. 上传接口立即返回 docId，后台异步处理。
2. 状态查询接口通过 docId 从 Redis 读取实时状态。
3. 消费端接收消息后：更新 PROCESSING -> 文本切块 -> 向量化持久化 -> SUCCESS/FAILED。

## 3. 待办事项（持续维护）

- [ ] 明确并固化 RAG 检索 Top-K、分块策略参数（长度/重叠）并补充到 docs/RAG_UPLOAD.md 与 docs/RAG_ASK.md。
- [x] 统一前端 API 封装层，集中处理 Result<T> 响应与 Token 注入。
- [ ] 增加关键流程测试：
  - [ ] Auth 注册/登录/重置密码接口测试
  - [ ] RAG ask 缓存命中与未命中路径测试
  - [ ] 文档向量化状态流转集成测试
- [x] 完成从 H2 到本地 MySQL 的迁移（配置、依赖、初始化 SQL）。
- [ ] 规范化文档更新流程：PR 检查项中增加"是否同步更新 docs 与 context.md"。

## 4. 每日操作记录索引（避免 context 过长）

- 2026-04-23: .github/prompts/daily/2026-04-23.md
- 2026-04-28: .github/prompts/daily/2026-04-28.md

## 5. 递进式对话日志索引（新增）

- 最近 5 条动态窗口: .github/prompts/recent-5.md
- 近 10 条 Stateful 摘要: .github/prompts/summary-10.md
- 工作流规范: .github/prompts/workflow-log.md

> 约定：
> 1. 每天新建一个文件：`.github/prompts/daily/YYYY-MM-DD.md`
> 2. 当天所有"需求/修改/对话结论"只写入当日日志
> 3. context.md 仅保留索引与关键结论，保证读取效率
> 4. recent-5.md 始终只保留最近 5 条（超出自动淘汰最旧项）
> 5. 每累计 10 条时更新 summary-10.md 的 Stateful 摘要

## 6. 与 docs 目录的关系

- docs/ 仍保留"按功能模块拆分"的详细说明：
  - docs/ARCHITECTURE.md
  - docs/AUTH.md
  - docs/RAG_ASK.md
  - docs/RAG_UPLOAD.md
- 本文件承担"跨模块总览 + 决策时间线 + 操作记录"职责。
- 推荐实践：
  1. 先改代码
  2. 再改对应 docs 模块文档
  3. 最后更新本 context.md（补充决策变化与对话操作记录）
