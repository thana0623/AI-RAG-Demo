# Data Schema: code-rag

> task-id: code-cache-mcp-v1
> status: locked

## 1. 核心数据模型

### Chunk（代码块）

```python
@dataclass
class Chunk:
    id: str                    # 唯一标识: md5(file_path + start_line + content)
    file_path: str             # 相对路径, 如 "src/auth/login.py"
    start_line: int            # 起始行号 (1-based)
    end_line: int              # 结束行号 (1-based)
    chunk_type: ChunkType      # 块类型
    name: str                  # 函数/类名, 普通块为空字符串
    content: str               # 代码内容
    language: str              # 语言标识: "python" | "java" | "unknown"
    metadata: dict             # 扩展字段, 预留

class ChunkType(str, Enum):
    FUNCTION = "function"      # 顶级函数
    CLASS = "class"            # 类定义
    METHOD = "method"          # 类内方法
    BLOCK = "block"            # 通用代码块 (无法识别为函数/类)
    DOCSTRING = "docstring"    # 模块/类文档字符串
```

**约束：**
- `id` 必须全局唯一，基于内容 hash 生成
- `file_path` 必须使用 `/` 分隔符（统一为 POSIX 风格）
- `start_line` >= 1，`end_line` >= `start_line`
- `content` 不可为空字符串
- `chunk_type` 必须为 ChunkType 枚举值之一

### IndexRecord（索引记录）

```python
@dataclass
class IndexRecord:
    chunk_id: str              # 关联 Chunk.id
    file_path: str             # 文件路径
    content_hash: str          # 内容 md5, 用于增量更新判断
    indexed_at: float          # Unix 时间戳
    embedding_hash: str        # embedding 向量的 hash, 用于缓存去重
```

### QueryResult（查询结果）

```python
@dataclass
class QueryResult:
    chunk: Chunk               # 匹配的代码块
    score: float               # 相关度分数 (0.0 ~ 1.0)
    rank: int                  # 排名 (1-based)
```

### QueryResponse（查询响应）

```python
@dataclass
class QueryResponse:
    results: list[QueryResult] # Top-K 结果
    token_usage: TokenUsage    # 本次查询 token 消耗
    query_time_ms: float       # 查询耗时 (毫秒)

@dataclass
class TokenUsage:
    input_tokens: int
    output_tokens: int
    model: str                 # 使用的模型名称
```

### IndexStatus（索引状态）

```python
@dataclass
class IndexStatus:
    total_files: int           # 已索引文件数
    total_chunks: int          # 总块数
    last_updated: str          # ISO 格式时间
    languages: dict[str, int]  # {"python": 800, "java": 200}
    persist_dir: str           # ChromaDB 存储路径
```

---

## 2. 配置 Schema

### CodeRAGConfig

```python
@dataclass
class CodeRAGConfig:
    # 项目路径
    project_path: str                          # 项目根目录

    # 存储配置
    persist_dir: str = ".code-rag"             # 索引存储目录 (相对于项目根)
    chroma_collection: str = "code_chunks"     # ChromaDB collection 名称

    # Embedding 配置
    embedding_model: str = "BAAI/bge-small-zh-v1.5"  # 模型名称
    embedding_device: str = "cpu"              # "cpu" | "cuda"
    embedding_cache_enabled: bool = True       # 是否启用 embedding 缓存

    # LLM 配置 (查询摘要, 可选)
    llm_enabled: bool = False                  # 是否启用 LLM 摘要
    llm_api_key: str = ""                      # DeepSeek API Key
    llm_base_url: str = "https://api.deepseek.com"  # API 地址
    llm_model: str = "deepseek-chat"           # 模型名称
    llm_max_tokens: int = 1024                 # 最大输出 token

    # 分块配置
    chunk_max_lines: int = 100                 # 单块最大行数
    chunk_min_lines: int = 3                   # 单块最小行数 (低于此值合并到上一块)

    # 查询配置
    query_top_k: int = 5                       # 默认返回 Top-K

    # Token 预算
    daily_token_limit: int = 500000            # 每日 token 上限
    on_exceed: str = "warn"                    # "warn" | "stop" | callback

    # 文件过滤
    exclude_dirs: list[str] = [                # 排除目录
        ".git", "node_modules", "__pycache__",
        ".venv", "venv", "dist", "build",
        ".code-rag", ".idea", ".vscode",
    ]
    exclude_extensions: list[str] = [          # 排除扩展名
        ".pyc", ".class", ".so", ".dll",
        ".jar", ".exe", ".png", ".jpg",
        ".gif", ".ico", ".svg", ".woff",
        ".woff2", ".ttf", ".eot",
    ]
    include_extensions: list[str] = [          # 索引扩展名 (v1)
        ".py", ".java",
    ]
```

**约束：**
- `persist_dir` 必须为相对路径，不可硬编码绝对路径
- `llm_api_key` 不可写入配置文件，必须从环境变量 `DEEPSEEK_API_KEY` 读取
- `embedding_model` 变更后必须全量重建索引 (embedding 维度可能变化)
- `exclude_dirs` 不可为空列表 (至少排除 .git)

---

## 3. ChromaDB Collection Schema

```
Collection: code_chunks
  ├── id: str              # Chunk.id
  ├── embedding: float[]   # BGE-small-zh-v1.5 输出 (384 维)
  ├── document: str        # Chunk.content
  └── metadata:
      ├── file_path: str
      ├── start_line: int
      ├── end_line: int
      ├── chunk_type: str
      ├── name: str
      └── language: str
```

**约束：**
- metadata 中所有值必须为 str/int/float/bool，不可嵌套
- embedding 维度由模型决定，BGE-small-zh-v1.5 = 384 维
- document 字段存储代码原文，最大 10000 字符 (超出截断)
