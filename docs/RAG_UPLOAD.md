# RAG文档上传与向量化模块 (RAG UPLOAD)

## 模块介绍
该模块提供大段文本或文档片段的内容向量化能力，使用到 RabbitMQ 进行异步队列解耦处理，避免同步执行阻塞主线程，并通过 Redis 记录任务执行的中间状态。

## 功能点及接口
- **开始异步长文本向量化**
  - **接口:** `POST /api/rag/document`
  - **流程:**
    1. 接收前端传入的 `content` 纯文本内容。
    2. 生成唯一的 `docId` (UUID) 并在 Redis 中创建此任务记录，状态初始置为 `PENDING`。
    3. 向 RabbitMQ 的 `document.vectorize.queue` 队列发送消息（包含 `docId` 及其 `content` 内容）。
    4. HTTP 会话立刻返回包含 `docId` 的响应给调用方。

- **获取异步处理状态**
  - **接口:** `GET /api/rag/status/{docId}`
  - **流程:** 前端携带返回的 `docId` 轮询本接口。接口直接读取 Redis 中的状态标记（如 `PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`），将实时状况返回前端。

## 前端实现与页面
- 页面位置：`frontend/src/pages/home/index.vue`
- 上传面板组件：`frontend/src/pages/home/components/UploadPanel.vue`
- 状态卡片组件：`frontend/src/components/business/DocumentStatusCard.vue`
- API 调用封装：`frontend/src/services/rag.ts`

## 前端交互流程（简要）
1. 用户输入内容并提交。
2. 成功后展示 `docId` 并默认状态为 `PENDING`。
3. 用户可点击“刷新状态”轮询任务进度。

- **异步消费者执行细节**
  - 使用 `@RabbitListener(queues = "${rag.mq.queue}")` 接收消息后触发真正的 AI 处理流。
  - 修改 Redis 中该文档对应的状态标志位（`PROCESSING`）。
  - 对文本应用 `TokenTextSplitter` 进行分割切块。
  - 将分块后生成的多个 Document 片段交由嵌入模型（EmbeddingClient）转向量并持久化存储。
  - 最后，若向量化持久化成功，将 Redis 中对应文档标记改为 `SUCCESS`；否则抛出异常将标记置为 `FAILED`。

---
`!Rule` 开发者提示：当修改文档上传、切块长度、队列参数或新增支持 PDF 导入等文档抽取功能时，请同步更新此文件。