# Architecture: code-rag

> task-id: code-cache-mcp-v1
> created: 2026-05-26T12:00:00+08:00

## 1. 项目目录结构

```
code-rag/
├── pyproject.toml              # 包定义 + 依赖
├── README.md                   # 使用文档
├── LICENSE                     # MIT
├── src/
│   └── code_rag/
│       ├── __init__.py         # 导出公共 API: CodeCache, TokenBudget, MCPServer
│       ├── config.py           # CodeRAGConfig 数据类 + 验证
│       ├── models.py           # Chunk, QueryResult, IndexStatus 等数据模型
│       ├── chunker.py          # 代码分块器 (Python/Java 解析)
│       ├── embedder.py         # BGE embedding 封装 + 缓存
│       ├── store.py            # ChromaDB 向量存储封装
│       ├── indexer.py          # 索引构建器 (全量 + 增量)
│       ├── query.py            # 语义查询引擎
│       ├── budget.py           # Token 预算器
│       ├── filters.py          # 文件过滤规则
│       ├── mcp_server.py       # MCP Server 入口 + 工具注册
│       └── parsers/
│           ├── __init__.py
│           ├── base.py         # Parser 基类
│           ├── python.py       # Python 文件解析器
│           ├── java.py         # Java 文件解析器
│           └── registry.py     # Parser 注册表 (按扩展名分发)
├── tests/
│   ├── __init__.py
│   ├── conftest.py             # 测试 fixtures
│   ├── test_chunker.py
│   ├── test_embedder.py
│   ├── test_store.py
│   ├── test_indexer.py
│   ├── test_query.py
│   ├── test_budget.py
│   ├── test_filters.py
│   ├── test_parsers/
│   │   ├── test_python.py
│   │   └── test_java.py
│   └── test_mcp_server.py
├── examples/
│   ├── basic_usage.py          # 最简使用示例
│   └── mcp_config.json         # Claude Code MCP 配置示例
└── docs/
    ├── schema.md               # → .github/prompts/plans/code-cache-mcp-v1/schema.md
    ├── api-spec.md             # → .github/prompts/plans/code-cache-mcp-v1/api-spec.md
    └── architecture.md         # → 本文件
```

---

## 2. 模块依赖关系

```
                    ┌─────────────────┐
                    │   mcp_server    │  MCP 协议层 (参数校验 + 调用转发)
                    └────────┬────────┘
                             │ 调用
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
        ┌──────────┐  ┌──────────┐  ┌──────────┐
        │ indexer  │  │  query   │  │  budget  │  业务层
        └────┬─────┘  └────┬─────┘  └──────────┘
             │              │
     ┌───────┼───────┐      │
     ▼       ▼       ▼      ▼
┌────────┐┌───────┐┌─────┐┌──────┐
│chunker ││filters││store││embedder│  核心层
└───┬────┘└───────┘└──┬──┘└──┬───┘
    │                 │      │
    ▼                 ▼      ▼
┌──────────────┐ ┌────────┐ ┌─────────────┐
│   parsers    │ │ChromaDB│ │sentence-tr  │  基础设施层
│(python/java) │ │        │ │ansformers   │
└──────────────┘ └────────┘ └─────────────┘
```

**依赖规则：**
- 上层可以依赖下层，下层不可依赖上层
- 同层模块之间不可循环依赖
- `mcp_server` 只依赖 `indexer`、`query`、`budget`
- `indexer` 依赖 `chunker`、`store`、`filters`
- `query` 依赖 `store`、`budget`
- `chunker` 依赖 `parsers`
- `parsers` 不依赖任何其他模块
- `store` 不依赖 `embedder`（embedding 由调用方传入）

---

## 3. 各模块职责

### 3.1 config.py — 配置管理

```python
class CodeRAGConfig:
    """所有配置项定义在 schema.md，此处只做加载和验证。"""
    @classmethod
    def from_env(cls, **overrides) -> "CodeRAGConfig": ...
    def validate(self) -> list[str]: ...  # 返回错误列表，空=合法
```

- 配置优先级：构造参数 > 环境变量 > 默认值
- `llm_api_key` 只从环境变量读取，不可通过参数传入

### 3.2 models.py — 数据模型

纯数据类定义，无任何业务逻辑。所有模型定义在 schema.md。

### 3.3 filters.py — 文件过滤

```python
def should_index(file_path: str, config: CodeRAGConfig) -> bool: ...
def list_indexable_files(project_path: str, config: CodeRAGConfig) -> list[str]: ...
```

- 规则：排除目录 → 排除扩展名 → 只保留允许扩展名
- 返回相对路径列表（POSIX 风格）

### 3.4 parsers/ — 文件解析器

```python
class BaseParser(ABC):
    @abstractmethod
    def parse(self, file_path: str, content: str) -> list[Chunk]: ...

class PythonParser(BaseParser): ...
class JavaParser(BaseParser): ...
```

**Python 解析策略（v1）：**
- 按顶层函数/类切分
- 类内方法独立切分
- 连续 import 语句合并为一个块
- 模块级 docstring 独立提取
- 空行分隔的代码块作为兜底

**Java 解析策略（v1）：**
- 按大括号匹配切分类/方法
- import 语句合并
- 类级注释关联到类块

### 3.5 chunker.py — 分块协调器

```python
class Chunker:
    def chunk_file(self, file_path: str, content: str) -> list[Chunk]: ...
    def chunk_project(self, project_path: str, files: list[str]) -> list[Chunk]: ...
```

- 根据文件扩展名选择对应 Parser
- 应用 chunk_max_lines / chunk_min_lines 约束
- 生成唯一 Chunk.id（内容 hash）

### 3.6 embedder.py — Embedding 封装

```python
class Embedder:
    def __init__(self, model: str, device: str): ...
    def embed(self, text: str) -> list[float]: ...
    def embed_batch(self, texts: list[str]) -> list[list[float]]: ...
```

- 使用 sentence-transformers 加载本地模型
- embed_batch 内部做 embedding hash 缓存（SQLite）
- 缓存命中时跳过模型推理

### 3.7 store.py — 向量存储

```python
class VectorStore:
    def __init__(self, persist_dir: str, collection: str): ...
    def add(self, chunks: list[Chunk], embeddings: list[list[float]]): ...
    def query(self, embedding: list[float], top_k: int, where: dict = None) -> list[QueryResult]: ...
    def delete_by_file(self, file_path: str): ...
    def count(self) -> int: ...
    def get_all_file_paths(self) -> set[str]: ...
```

- ChromaDB 本地持久化
- where 参数支持 metadata 过滤（language、file_path 前缀）
- 每次 add 自动覆盖同 id 的记录（upsert 语义）

### 3.8 indexer.py — 索引构建

```python
class Indexer:
    def __init__(self, config: CodeRAGConfig): ...
    def build(self) -> IndexStatus: ...       # 全量构建
    def refresh(self) -> IndexStatus: ...     # 增量更新 (git diff)
```

**build 流程：**
1. `list_indexable_files()` 获取文件列表
2. `chunker.chunk_project()` 分块
3. `embedder.embed_batch()` 向量化
4. `store.add()` 写入 ChromaDB
5. 写入索引元数据（文件列表 + hash）

**refresh 流程：**
1. `git diff --name-only HEAD` 获取变更文件列表
2. 对变更文件：删除旧 chunks → 重新分块 → 向量化 → 写入
3. 对新增文件：同 build 流程
4. 对删除文件：`store.delete_by_file()`

### 3.9 query.py — 查询引擎

```python
class QueryEngine:
    def __init__(self, config: CodeRAGConfig): ...
    def query(self, question: str, top_k: int = 5, **filters) -> QueryResponse: ...
```

**流程：**
1. `budget.check()` 检查预算
2. `embedder.embed(question)` 向量化查询
3. `store.query()` 向量检索
4. (可选) LLM 摘要生成
5. `budget.record()` 记录消耗
6. 返回 QueryResponse

### 3.10 budget.py — Token 预算器

```python
class TokenBudget:
    def __init__(self, daily_limit: int, on_exceed: str = "warn"): ...
    def check(self) -> bool: ...           # 是否还有预算
    def record(self, tokens: int): ...     # 记录消耗
    def remaining(self) -> int: ...
    def is_exceeded(self) -> bool: ...
    def reset(self): ...                   # 手动重置
```

- 按自然日重置（UTC 0 点）
- 超限行为：warn=仅日志告警 / stop=抛异常
- 持久化到 `.code-rag/budget.json`

### 3.11 mcp_server.py — MCP 入口

```python
def create_server(config: CodeRAGConfig) -> Server: ...
```

- 注册 5 个 MCP 工具（index / query / status / refresh / delete）
- 参数校验 + 错误格式化
- 调用转发到 indexer / query / budget

---

## 4. ADR（架构决策记录）

### ADR-1: 使用 ChromaDB 而非 Milvus/Qdrant

**状态：** 已采纳

**背景：** 需要一个向量数据库存储代码块的 embedding。

**决策：** 使用 ChromaDB

**理由：**
- pip install 即用，零外部依赖
- 本地文件持久化，重启不丢数据
- Python 原生 SDK，集成最简单
- MVP 阶段数据量 <100 万条，ChromaDB 性能足够

**风险：** 数据量超过 100 万条后性能可能下降。缓解：store 层做好接口抽象，可替换为 Qdrant。

### ADR-2: BGE 本地 Embedding 而非 API

**状态：** 已采纳

**背景：** 需要将代码文本转换为向量。

**决策：** 使用 BGE-small-zh-v1.5 本地模型

**理由：**
- 零 API 成本
- 中文代码注释/变量名效果好
- 95MB 模型，CPU 可运行
- 不依赖外部服务

**风险：** CPU 推理速度较慢（~50ms/条）。缓解：embed_batch 批量处理 + embedding 缓存。

### ADR-3: 通用切块策略而非 AST 解析

**状态：** 已采纳

**背景：** 需要将代码文件切分为语义块。

**决策：** v1 使用基于缩进/空行/大括号的通用切块策略，不做 AST 解析

**理由：**
- Python 和 Java 语法差异大，AST 解析需要每种语言写独立解析器
- 通用策略对函数/类级别的切分已经足够准确
- 实现简单，1-2 周可完成 MVP

**风险：** 边界情况（嵌套类、装饰器、匿名函数）切分不够精确。缓解：v2 引入 tree-sitter AST 解析。

### ADR-4: MCP 而非 CLI / REST API

**状态：** 已采纳

**背景：** 需要提供接口给 Claude Code / PMCP 调用。

**决策：** 作为 MCP Server 暴露工具

**理由：**
- Claude Code 原生支持 MCP 协议
- PMCP 生态已有 MCP 基础设施
- 无需额外 HTTP 服务，stdio 传输
- 用户配置 `.claude/settings.json` 即可接入

**风险：** MCP 协议演进可能带来兼容性问题。缓解：使用官方 Python SDK。

### ADR-5: store 与 embedder 解耦

**状态：** 已采纳

**背景：** 向量存储和 embedding 计算是否耦合。

**决策：** 完全解耦，store 只接收 embedding 向量，不调用 embedder

**理由：**
- 单一职责：store 管存储，embedder 管计算
- 便于测试：mock embedding 直接注入
- 便于替换：换 embedding 模型不影响 store 层

---

## 5. 依赖清单

```toml
[project]
name = "code-rag"
version = "0.1.0"
requires-python = ">=3.10"
dependencies = [
    "chromadb>=0.4.0",
    "sentence-transformers>=2.2.0",
    "mcp>=1.0.0",
    "gitpython>=3.1.0",
]

[project.optional-dependencies]
llm = [
    "openai>=1.0.0",      # DeepSeek 兼容 OpenAI SDK
]
dev = [
    "pytest>=7.0.0",
    "pytest-asyncio>=0.21.0",
]
```

| 依赖 | 用途 | 版本 |
|------|------|------|
| chromadb | 向量数据库 | >=0.4.0 |
| sentence-transformers | BGE 本地 embedding | >=2.2.0 |
| mcp | MCP Server SDK | >=1.0.0 |
| gitpython | git diff 检测 | >=3.1.0 |
| openai (可选) | DeepSeek API 调用 | >=1.0.0 |

---

## 6. PMCP 集成方案

### SessionStart 阶段

```bash
# .prompts-mcp/adapters/claude-code/session-start.sh 中追加：
# 检测是否已有索引, 没有则自动构建
if [ ! -d "$PROJECT_ROOT/.code-rag" ]; then
  python -m code_rag index "$PROJECT_ROOT"
fi
```

### AI 工作中

Claude Code 通过 MCP 调用 `code_cache_query`：
```json
{"name": "code_cache_query", "arguments": {"question": "认证逻辑", "top_k": 5}}
```

### SessionEnd 阶段

```bash
# .prompts-mcp/hooks/session-end.sh 中追加：
python -m code_rag refresh "$PROJECT_ROOT"
```

### Claude Code 配置

```json
// .claude/settings.json
{
  "mcpServers": {
    "code-rag": {
      "command": "python",
      "args": ["-m", "code_rag", "mcp"],
      "env": {
        "DEEPSEEK_API_KEY": "${DEEPSEEK_API_KEY}"
      }
    }
  }
}
```
