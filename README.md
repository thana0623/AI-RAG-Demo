# AI-RAG-Demo

一个基于 Spring Boot + Vue 的 RAG 示例项目，包含 RabbitMQ 异步向量化与 Redis 状态/缓存能力。

## 本地配置（避免提交敏感信息）

仓库内已提供脱敏模板：

- `backend/src/main/resources/application-local.yml.example`

使用方式：

1. 复制模板并创建你自己的本地配置文件：
   - `cp backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml`
2. 将 `application-local.yml` 中的 RabbitMQ / Redis 连接信息改成你本机真实值。
3. 运行后端时启用 `local` profile（例如：`--spring.profiles.active=local`）。

> `application-local.yml` 已加入 `.gitignore`，不会被提交到仓库，从而避免泄露本地账号密码。
