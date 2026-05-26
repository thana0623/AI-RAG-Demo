> task-id: code-cache-mcp-v1
> created: 2026-05-26T11:00:00+08:00
> status: confirmed

# Focus Spec: 代码缓存 MCP 模块 (v1)

## 1. 场景还原

开发者使用 Claude Code + PMCP 系统进行项目开发。每次会话启动时，AI 需要理解项目上下文（代码结构、模块关系、关键函数），目前的做法是全量读取项目文件，导致大量 token 消耗。

**核心痛点：** AI 每次开新会话都要"重新认识"项目，烧 token 且效率低。

**解决方案：** 构建代码缓存模块，预先将项目代码索引为向量库，AI 通过语义查询按需获取相关代码片段，而非全量读取。

**集成形态：** Python MCP Server 模块，通过 `pip install code-rag` 安装，作为 PMCP 生态的组成部分，接入 Claude Code 工作流。

**用户角色：** 使用 Claude Code + PMCP 进行开发的程序员。

---

## 2. 核心业务边界

### IN（肯定在范围内）

- **代码分块（Chunking）**：将源代码文件按函数/类/逻辑块切分，每个块保留文件路径、行号、类型（function/class/method/module-docstring）元数据
- **向量化索引**：使用 BGE-small-zh-v1.5 本地 embedding（零 API 成本），存入 ChromaDB 本地持久化
- **语义查询**：自然语言输入 → 返回 Top-K 相关代码片段（含文件路径、行号、代码内容、相关度分数）
- **增量更新**：基于 git diff 检测变更文件，只重新索引变更部分，非全量重建
- **MCP Server 接口**：暴露 4 个 MCP 工具（index / query / status / refresh），可直接被 Claude Code 调用
- **Token 预算器**：记录每次查询消耗，支持每日上限配置，超限告警回调
- **多语言 v1**：Python 文件解析（按缩进+空行切块），Java 文件解析（按大括号切块）
- **pip install 即用**：标准 Python 包，`pyproject.toml` 打包

### OUT（肯定不在范围内）

- AST 级语法解析（v1 不做，v2 考虑）
- 知识图谱 / 调用链分析（v1 不做）
- 实时文件监听（tail -f / inotify）
- Web UI / 可视化仪表盘
- 分布式向量库（Milvus/Qdrant）
- 非代码文件（.md / .json / .yml 等配置文件不索引）
- 代码生成 / 自动补全
- 多租户 / 权限管理

---

## 3. 禁止触碰黑名单

- 禁止使用 AST 解析器（v1 用通用策略：按缩进/空行/大括号切块，不做语法树解析）
- 禁止全量重建索引（必须基于 git diff 增量更新，除非用户显式传入 `force=True`）
- 禁止索引 `.git/`、`node_modules/`、`__pycache__/`、`.venv/`、`dist/`、`build/` 目录
- 禁止索引二进制文件（.pyc / .class / .so / .dll / .jar / .exe）
- 禁止将 ChromaDB 路径硬编码（必须支持用户配置，默认 `.code-rag/chromadb/`）
- 禁止忽略 token 计数（所有查询必须经过 token 预算器）
- 禁止在模块内部管理 API Key / 模型路径（必须由用户传入或环境变量读取）
- 禁止 embedding 模型与 LLM 模型耦合配置（两者独立）
- 禁止每次查询重新加载向量库（必须复用持久化连接）
- 禁止将 MCP 工具与业务逻辑耦合（MCP 层只做参数解析和调用转发，核心逻辑在 service 层）

---

## 4. 核心测试断言清单

```python
# === 代码分块 ===
chunks = chunk_file("sample.py", python_code_text)
assert len(chunks) > 0
assert chunks[0].file_path == "sample.py"
assert chunks[0].start_line >= 1
assert chunks[0].chunk_type in ("function", "class", "method", "module-docstring", "block")
assert chunks[0].content  # 非空

# Python 函数切分
py_chunks = chunk_file("auth.py", "def login(user):\n    return user\n\ndef logout():\n    pass\n")
assert len(py_chunks) == 2
assert py_chunks[0].name == "login"
assert py_chunks[1].name == "logout"

# Java 方法切分
java_chunks = chunk_file("Service.java", "public class Service {\n    public void doWork() {}\n    private void helper() {}\n}\n")
assert len(java_chunks) >= 2

# === 文件过滤 ===
assert should_index("src/main.py") == True
assert should_index("node_modules/pkg/index.js") == False
assert should_index(".git/config") == False
assert should_index("image.png") == False
assert should_index("dist/bundle.js") == False

# === 向量索引 ===
store = VectorStore(persist_dir="/tmp/test-chromadb")
store.add(chunks=[Chunk(file_path="a.py", content="def login(): pass", start_line=1, chunk_type="function")])
assert store.count() == 1
results = store.query("用户登录", top_k=5)
assert len(results) >= 1
assert results[0].file_path == "a.py"
assert results[0].score > 0

# === Embedding 缓存 ===
call_count = 0
original_embed = embedder.embed
def counting_embed(text): nonlocal call_count; call_count += 1; return original_embed(text)
embedder.embed = counting_embed
store.add(chunks=[Chunk(file_path="b.py", content="same code", start_line=1, chunk_type="function")])
store.add(chunks=[Chunk(file_path="c.py", content="same code", start_line=1, chunk_type="function")])
assert call_count == 1  # 第二次命中缓存

# === 增量更新 ===
indexer.build_index("/project")  # 全量
old_count = store.count()
# 修改一个文件后
indexer.refresh("/project")  # 增量
assert store.count() >= old_count  # 至少不减少

# === MCP 工具 ===
result = mcp_server.call_tool("code_cache_index", {"project_path": "/project"})
assert result.success == True
assert result.files_indexed > 0

result = mcp_server.call_tool("code_cache_query", {"question": "认证逻辑", "top_k": 5})
assert result.results
assert result.token_usage

result = mcp_server.call_tool("code_cache_status", {})
assert result.total_chunks > 0
assert result.last_updated

# === Token 预算器 ===
budget = TokenBudget(daily_limit=50000)
budget.record(30000)
assert budget.remaining() == 20000
budget.record(25000)
assert budget.is_exceeded() == True
assert budget.on_exceed_triggered == True

# === 包安装 ===
import code_rag
assert hasattr(code_rag, 'CodeCache')
assert hasattr(code_rag, 'TokenBudget')
assert hasattr(code_rag, 'MCPServer')
```

---

## 附录：技术选型

| 组件 | 选型 | 理由 |
|---|---|---|
| 向量数据库 | ChromaDB | 零配置，pip install，本地持久化 |
| Embedding | BGE-small-zh-v1.5（本地） | 零 API 成本，中文优化，95MB |
| LLM（查询摘要） | DeepSeek API（OpenAI 兼容） | ¥1/百万 tokens，可选关闭 |
| MCP SDK | mcp（Python） | 官方 Python MCP SDK |
| 包管理 | pyproject.toml + hatchling | 现代 Python 标准 |
| 增量更新 | git diff 检测 | 只重索引变更文件 |
| 缓存 | SQLite（embedding hash 去重） | 零依赖，本地存储 |

### Token 成本预估（1000 文件项目）

| 阶段 | Token 量 | 成本 |
|---|---|---|
| 索引阶段（embedding） | ~50 万 | ¥0（本地 BGE） |
| 索引阶段（LLM 摘要，可选） | ~50 万 input + ~10 万 output | ¥0.52（一次性） |
| 单次查询 | ~3000 input + ~500 output | ¥0.004 |
| 日常使用（50 次查询/天） | ~15 万 | ¥0.2/天 |
| **月合计** | - | **~¥6/月** |

### 与 PMCP 集成架构

```
Claude Code
  │
  ├── pmcp start (SessionStart hook)
  │     └── code_cache.index  ← 自动构建索引
  │
  ├── AI 工作中
  │     └── code_cache.query  ← 按需语义检索
  │
  └── SessionEnd hook
        └── code_cache.refresh ← 增量更新
```
