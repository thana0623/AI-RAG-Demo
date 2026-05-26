# MCP Tool API Spec: code-rag

> task-id: code-cache-mcp-v1
> status: locked

## 概述

本模块作为 MCP Server 暴露 4 个工具，供 Claude Code / PMCP 调用。

传输协议：stdio（标准 MCP 协议）
工具前缀：`code_cache_`

---

## Tool 1: code_cache_index

**用途：** 扫描项目目录，构建代码索引

### 请求

```json
{
  "name": "code_cache_index",
  "arguments": {
    "project_path": "/absolute/path/to/project",
    "force": false,
    "languages": ["python", "java"]
  }
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| project_path | string | 是 | - | 项目根目录绝对路径 |
| force | boolean | 否 | false | true=全量重建, false=增量更新 |
| languages | string[] | 否 | ["python", "java"] | 索引的语言 |

### 响应

```json
{
  "success": true,
  "data": {
    "files_indexed": 120,
    "chunks_created": 856,
    "chunks_updated": 23,
    "chunks_deleted": 5,
    "duration_ms": 3200,
    "languages": {
      "python": 95,
      "java": 25
    }
  }
}
```

### 错误

```json
{
  "success": false,
  "error": {
    "code": "PATH_NOT_FOUND",
    "message": "Project path does not exist: /invalid/path"
  }
}
```

| 错误码 | 说明 |
|--------|------|
| PATH_NOT_FOUND | 项目路径不存在 |
| PATH_NOT_DIRECTORY | 路径不是目录 |
| NO_FILES_FOUND | 目录下无可索引文件 |
| EMBEDDING_ERROR | Embedding 模型加载失败 |
| CHROMADB_ERROR | ChromaDB 写入失败 |

---

## Tool 2: code_cache_query

**用途：** 语义查询相关代码片段

### 请求

```json
{
  "name": "code_cache_query",
  "arguments": {
    "question": "用户认证逻辑是怎么实现的",
    "top_k": 5,
    "language_filter": null,
    "file_filter": null
  }
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| question | string | 是 | - | 自然语言查询 |
| top_k | integer | 否 | 5 | 返回结果数量 (1-20) |
| language_filter | string | 否 | null | 语言过滤: "python" / "java" |
| file_filter | string | 否 | null | 文件路径前缀过滤: "src/auth/" |

### 响应

```json
{
  "success": true,
  "data": {
    "results": [
      {
        "file_path": "src/auth/login.py",
        "start_line": 15,
        "end_line": 42,
        "chunk_type": "function",
        "name": "authenticate",
        "content": "def authenticate(username, password):\n    ...",
        "score": 0.89,
        "rank": 1
      }
    ],
    "token_usage": {
      "input_tokens": 150,
      "output_tokens": 0,
      "model": "BAAI/bge-small-zh-v1.5"
    },
    "query_time_ms": 45
  }
}
```

### 错误

| 错误码 | 说明 |
|--------|------|
| INDEX_NOT_FOUND | 索引不存在，需先调用 index |
| QUERY_FAILED | 向量查询失败 |
| TOKEN_BUDGET_EXCEEDED | 每日 token 预算超限 |

---

## Tool 3: code_cache_status

**用途：** 查看索引状态

### 请求

```json
{
  "name": "code_cache_status",
  "arguments": {}
}
```

无必填参数。

### 响应

```json
{
  "success": true,
  "data": {
    "total_files": 120,
    "total_chunks": 856,
    "last_updated": "2026-05-26T12:00:00+08:00",
    "languages": {
      "python": 680,
      "java": 176
    },
    "persist_dir": "/project/.code-rag",
    "embedding_model": "BAAI/bge-small-zh-v1.5",
    "token_budget": {
      "daily_limit": 500000,
      "used_today": 12500,
      "remaining": 487500
    }
  }
}
```

### 错误

| 错误码 | 说明 |
|--------|------|
| INDEX_NOT_FOUND | 索引不存在 |

---

## Tool 4: code_cache_refresh

**用途：** 基于 git diff 增量更新索引

### 请求

```json
{
  "name": "code_cache_refresh",
  "arguments": {
    "project_path": "/absolute/path/to/project"
  }
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| project_path | string | 是 | - | 项目根目录绝对路径 |

### 响应

```json
{
  "success": true,
  "data": {
    "files_changed": 3,
    "chunks_added": 12,
    "chunks_updated": 8,
    "chunks_deleted": 2,
    "duration_ms": 800
  }
}
```

### 错误

| 错误码 | 说明 |
|--------|------|
| NOT_GIT_REPOSITORY | 目录不是 git 仓库 |
| GIT_ERROR | git 命令执行失败 |
| INDEX_NOT_FOUND | 索引不存在，需先调用 index |

---

## Tool 5: code_cache_delete

**用途：** 删除索引

### 请求

```json
{
  "name": "code_cache_delete",
  "arguments": {
    "project_path": "/absolute/path/to/project",
    "confirm": true
  }
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| project_path | string | 是 | - | 项目根目录 |
| confirm | boolean | 是 | - | 必须为 true 才执行删除 |

### 响应

```json
{
  "success": true,
  "data": {
    "deleted": true,
    "chunks_removed": 856
  }
}
```

---

## 通用约束

1. 所有路径参数必须为绝对路径
2. 所有响应必须包含 `success: boolean` 字段
3. 失败时必须包含 `error.code` 和 `error.message`
4. `code_cache_query` 每次调用必须记录 token 使用量
5. 当 token 预算超限时，`code_cache_query` 返回 `TOKEN_BUDGET_EXCEEDED` 错误
6. 所有时间字段使用 ISO 8601 格式
7. MCP 工具层只做参数校验和调用转发，不含业务逻辑
