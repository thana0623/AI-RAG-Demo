# AI-RAG-Demo

一个基于 Spring Boot + Vue 3 的 RAG（检索增强生成）示例项目，支持文档上传、向量化、智能问答，集成 RabbitMQ 异步处理与 Redis 缓存。

## 技术栈

**后端**
- Java / Spring Boot
- Spring AI（向量存储与嵌入）
- RabbitMQ（异步文档向量化）
- Redis（缓存与状态管理）
- MySQL（用户与文档数据）

**前端**
- Vue 3 + TypeScript
- Vite
- Pinia（状态管理）
- Vue Router
- Arco Design Vue

## 项目结构

```
├── backend/          # Spring Boot 后端
│   ├── src/          # Java 源码
│   └── sql/          # 数据库初始化脚本
├── frontend/         # Vue 3 前端
│   └── src/          # 前端源码
├── docker-compose.yml
└── docs/
```

## 快速开始

### 1. 启动基础服务

```bash
docker-compose up -d
```

启动 MySQL、Redis、RabbitMQ 三个容器。

### 2. 后端

```bash
cd backend
# 复制并修改本地配置
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# 编辑 application-local.yml，填入本地 RabbitMQ / Redis 连接信息
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

## 本地配置

`backend/src/main/resources/application-local.yml` 已加入 `.gitignore`，不会提交到仓库。

首次使用时复制模板并填入本地连接信息：

```bash
cp backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml
```

## 功能

- 用户注册 / 登录 / 密码重置
- 文档上传与异步向量化
- 基于 RAG 的智能问答

## License

[MIT](LICENSE)
