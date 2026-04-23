# 整体架构及目录结构规范 (ARCHITECTURE)

## 核心技术栈
- 框架基底: **Spring Boot 3.2.4** 和 **Java 17**
- 前端框架: **Vue 3 (Vite 驱动) + Arco Design**
- 状态管理: **Pinia**
- 路由管理: **Vue Router 4**
- HTTP 客户端: **Axios（统一封装 API 层）**
- 消息代理: **RabbitMQ (AMQP)**，异步文档处理解耦
- 全局混存: **Redis**，支持会话 Token 管理、RAG 对话结果缓存，及任务队列的进度标记
- 关系数据库: **MySQL 8.x**，当前用作用户体系持久层（本地默认库名 `rag_demo`）。
- 业务依赖件: **Spring AI**，简化通过 OpenAI 协议接入向量数据库（默认内存方案）和 Chat 接口的操作。

## 后层代码 (Backend - Maven) 结构规范分层
严格依照经典 **MVC** (Model-View-Controller) 架构分层组织：
- `controller/`：控制器层。处理各模块的入参/出参接口。
- `service/` 及 `service.impl/`：业务层。包括复杂的认证、发送邮件、RAG 交互、内容清洗加工。
- `model/` 及其下的 `request`/`response`/`entity` 包：实体定义包。规定外部请求 (`Request`) 结构，和数据内部响应及数据库关联实体对象 (`Entity`) 以及通过统一包装泛型类返回 (`Result.java`) 的设计。
- `repository/`：持久层。存放继承自 `JpaRepository` 的各类数据交互接口。
- `mq/`：专门的消息消费者服务。处理从 Rabbit 收到数据后的分发落库及业务传递逻辑。
- `config/`：配置及注册中心。存放涉及向量库、交换机、队列注入等第三方 SDK 和配置的 Bean 的地方。

## 前端代码 (Frontend - Vue 3) 分层架构

```
frontend/src/
├── api/              # API 请求层（统一封装）
│   ├── request.js    # Axios 实例 + 拦截器（Token 注入、错误处理）
│   ├── auth.js       # 认证相关 API（登录/注册/验证码/重置密码）
│   └── rag.js        # RAG 相关 API（文档上传/状态查询/问答）
├── router/           # 路由配置
│   └── index.js      # 路由定义 + 导航守卫（未登录跳转登录页）
├── stores/           # Pinia 状态管理
│   └── auth.js       # 用户认证状态（Token/用户信息/登录/登出）
├── views/            # 页面组件
│   ├── Login.vue     # 登录页（邮箱/用户名 + 密码）
│   ├── Register.vue  # 注册页（邮箱 + 用户名 + 密码 + 验证码）
│   ├── ForgotPassword.vue  # 找回密码页（邮箱 + 验证码 + 新密码）
│   └── Home.vue      # RAG 主页面（文档上传 + 知识问答）
├── App.vue           # 根组件（含路由视图 + 登录状态初始化）
└── main.js           # 入口（挂载 Pinia + Router + Arco Design）
```

### 前端架构说明

1. **API 层 (`api/`)**：统一通过 Axios 实例发送请求，自动注入 Bearer Token，统一处理 `Result<T>` 响应结构。
2. **路由层 (`router/`)**：使用 Vue Router 4 的 History 模式，通过导航守卫控制未登录用户跳转到登录页。
3. **状态管理层 (`stores/`)**：使用 Pinia 管理全局认证状态，Token 持久化到 localStorage。
4. **视图层 (`views/`)**：每个页面独立组件，通过 `api/` 层调用后端接口，通过 `stores/` 获取用户状态。

## 规范与最佳实践
- 控制器层 (`controller/`) 应避免混入复杂业务逻辑，且保证其方法均通过 `Result<T>` 作为统一外层包裹输出给前端。
- 前端应通过统一的 API 层（`api/request.js`）适配后端的 `code`, `data`, `message` 响应模型。
- 前端所有 API 调用先统一引入 `api/` 层，再在组件中使用，避免直接在组件中编写请求逻辑。

## 配置管理规范（新增）
- `backend/src/main/resources/application.yml` 为可提交的公共配置文件，统一使用环境变量占位符，避免硬编码敏感信息。
- `backend/src/main/resources/application-local.yml` 为本地私有覆盖配置，不进入 Git（通过 `.gitignore` 管理）。
- `backend/src/main/resources/application-local.yml.example` 为团队共享模板，用于快速初始化本地开发环境。
- 中间件本地默认约定：RabbitMQ 使用 `5672`（AMQP）与 `15672`（控制台），Redis 使用 `6379`。

---
`!Rule` 开发者提示：后续涉及到引入新的技术栈、改写包层结构分封，均要回查和修改本模块的架构说明。
