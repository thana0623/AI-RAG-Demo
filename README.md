# AI-RAG-Demo

一个基于 Spring Boot + Vue 3 的 RAG 演示项目，包含：
- 用户注册/登录/邮箱验证码能力
- 文档上传后通过 RabbitMQ 异步向量化
- Redis 任务状态与问答缓存
- 基于 Spring AI 的问答能力

## 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- Docker Desktop（用于 RabbitMQ/Redis）

## 2. 一键启动基础依赖（RabbitMQ + Redis）

在项目根目录执行：

```bash
docker compose up -d rabbitmq redis
```

启动后默认端口：
- RabbitMQ AMQP：`5672`
- RabbitMQ 管理台：`15672`
- Redis：`6379`

默认账号密码（已和后端本地模板一致）：
- RabbitMQ：`admin / 123456`
- Redis：`123456`

## 3. 后端配置说明（支持脱敏提交）

后端配置采用两层：
- `backend/src/main/resources/application.yml`：可提交到 Git 的通用配置（环境变量占位）
- `backend/src/main/resources/application-local.yml`：你的本地私有配置（已加入 `.gitignore`，不会提交）

### 本地配置步骤

1. 复制模板文件：

```bash
cp backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml
```

Windows PowerShell 可用：

```powershell
Copy-Item backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml
```

2. 按需修改 `application-local.yml` 中的本地账号、密码、OpenAI Key、邮箱配置。

## 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

## 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

## 6. 常见问题

- RabbitMQ 连接失败：确认后端连的是 `5672`，不是管理页面端口 `15672`
- Redis 认证失败：确认 `application-local.yml` 中 `spring.data.redis.password` 与容器一致
- OpenAI 调用失败：检查 `OPENAI_API_KEY` 或 `application-local.yml` 中 `spring.ai.openai.api-key`

## 7. Git 提交建议（避免泄露本地信息）

- 仅提交：`application.yml`、`application-local.yml.example`
- 不提交：`application-local.yml`
- 提交前可执行：

```bash
git status
```

确认没有本地敏感信息文件进入暂存区。
