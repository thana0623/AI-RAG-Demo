# 身份认证模块 (AUTH)

## 模块介绍
该模块提供完整的用户身份认证功能，包括注册、登录、通过邮箱找回密码等。所有相关接口和信息应参考本文档并保持同步更新。

## 功能点及接口
- **注册**
  - **接口:** `POST /api/auth/register`
  - **流程:** 输入邮箱、用户名、密码和邮箱验证码，验证通过后写入数据库完成注册。
  - **异常:** 验证码无效或已过期返回错误码 1003；用户名或邮箱已存在返回错误码 1004。

- **登录**
  - **接口:** `POST /api/auth/login`
  - **流程:** 输入用户名/邮箱与密码，通过后生成并返回一个唯一的 Token（通过 Redis 存储，有效期 7 天）。前端凭借此 Token 进行其他操作请求。
  - **异常:** 用户名/邮箱或密码错误返回错误码 1005。

- **发送验证码**
  - **接口:** `POST /api/auth/send-code`
  - **流程:** 支持传入业务类型 `REGISTER`（注册） 或 `RESET`（找回密码）。通过邮箱的 SMTP 向相应的邮箱发送一个 6 位验证码（通过 Redis 存储并设过期时间 10 分钟）。
  - **异常:** 邮箱已被注册返回错误码 1001；邮箱未注册返回错误码 1002。

- **找回及重置密码**
  - **接口:** `POST /api/auth/reset-password`
  - **流程:** 输入邮箱、新密码以及邮箱验证码，验证有效后覆盖原来的密码记录。
  - **异常:** 验证码无效或已过期返回错误码 1003；用户不存在返回错误码 1006。

- **获取当前用户信息**
  - **接口:** `GET /api/auth/current`
  - **流程:** 请求头携带 `Authorization: Bearer <Token>` 访问，返回除去密码字段以外的用户信息。
  - **异常:** Token 无效或已过期返回错误码 1007。

## 统一错误码
| 错误码 | 说明 |
|-------|------|
| 1001 | 该邮箱已被注册 |
| 1002 | 该邮箱未注册 |
| 1003 | 验证码无效或已过期 |
| 1004 | 用户名或邮箱已存在 |
| 1005 | 用户名/邮箱或密码错误 |
| 1006 | 用户不存在 |
| 1007 | Token 无效或已过期 |

## 异常处理机制
- Controller 层不再使用 try-catch 处理异常，统一由 `GlobalExceptionHandler` 捕获。
- Service 层通过抛出 `BusinessException(ErrorCode)` 触发异常处理。
- 所有错误消息均为中文，便于开发调试。

## 前端实现与页面
- 页面路由
  - 登录页：`/login`
  - 注册页：`/register`
  - 找回密码：`/forgot-password`
- 前端页面位置：
  - 登录页组件：`frontend/src/pages/auth/login/index.vue`
  - 注册页组件：`frontend/src/pages/auth/register/index.vue`
  - 找回密码组件：`frontend/src/pages/auth/forgot-password/index.vue`
- API 调用封装：`frontend/src/services/auth.ts`
- 认证状态管理：`frontend/src/store/modules/user.ts`

## 前端交互流程（简要）
- 登录：提交 `identifier + password`，成功后保存 Token 并拉取用户信息。
- 注册：先发送验证码，再提交 `email + username + password + code` 完成注册。
- 找回密码：发送验证码后提交 `email + code + newPassword` 完成重置。

## 数据库设计
使用 `sys_user` 表存储，包含 `id`, `email`, `username`, `password` 字段。密码利用 `SHA-256 + Salt` 进行不可逆散列加密。

## MySQL 索引与字符集规范（新增）

### 字符集与排序规则
- 数据库与业务表统一使用：`utf8mb4`
- 排序规则统一使用：`utf8mb4_unicode_ci`
- 目的：确保邮箱、用户名、国际化字符（含 emoji）在存储和比较时行为一致。

### `sys_user` 表索引规范
- 主键：`PRIMARY KEY (id)`
- 唯一索引：`uk_sys_user_email (email)`
- 唯一索引：`uk_sys_user_username (username)`
- 约束要求：`email`、`username`、`password` 均为 `NOT NULL`

### 设计约束说明
- `email` 与 `username` 必须全局唯一，依赖数据库唯一索引兜底，避免并发注册出现重复写入。
- 应用层可先做格式与重复校验，但最终一致性以数据库约束为准。
- 涉及字段扩展（例如手机号、昵称）时，需在本文件补充索引策略与字符集策略后再落库。

---
`!Rule` 开发者提示：当修改 Authentication 相关的业务代码或数据库字段时，请同步更新此文件的记录。
