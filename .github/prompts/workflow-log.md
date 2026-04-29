# 递进式 AI 对话日志工作流

## 目标
- 保留最近 5 条对话与操作（动态窗口）。
- 每累计近 10 条，产出一次 Stateful Markdown 摘要。
- 每条对话统一执行：清洗 -> 提取 -> 归档/压缩。

## 文件职责
- .github/prompts/daily/YYYY-MM-DD.md
  - 保存当日全量原始记录（可读、可追溯）。
- .github/prompts/recent-5.md
  - 保存最近 5 条清洗后的结构化记录。
- .github/prompts/summary-10.md
  - 保存 10 条窗口的有状态摘要与窗口元信息。
- .github/prompts/context.md
  - 仅保留索引、全局技术栈、关键决策与待办。

## 执行纪律

1. 每次处理新请求，先读取 `.github/prompts/context.md`，确认当前全局状态。
2. 再按任务类型加载最相关的 prompt 文件，避免只看局部上下文就直接动手。
3. 如果需求表达模糊、缺少边界、缺少验收条件，先追问；一轮不够就继续追问，直到需求可以被明确执行。
4. 在需求明确前，不允许直接设计实现方案，更不允许直接写代码。
5. 只有在明确“要做什么、为什么做、做到什么程度”之后，才进入设计与编码。

## 固定追问清单

当需求不清晰时，优先按以下顺序追问：

1. 你要解决的具体问题是什么？
2. 期望输出是什么，成功标准是什么？
3. 这次变更影响哪些文件、模块或页面？
4. 是否有不能改动的约束、技术选型或业务边界？
5. 是否需要补充设计稿、接口契约、数据结构或测试要求？

如果用户回答仍然模糊，继续围绕缺失项追问，不要自己补全。

## 单条处理流程（每次对话）
1. 清洗
- 删除语气词、寒暄、开场白。
- 仅保留需求事实、约束、验收条件。

2. 提取
- 代码变更: 涉及文件与核心改动点。
- 技术决策: 新增/变更/废弃的决策。
- 待办事项: 未完成且可执行的下一步。

3. 入库
- 先追加到 daily 当日文件。
- 再写入 recent-5 作为最新一条 Entry。
- 若 recent-5 超过 5 条，删除最旧一条，保持 5 条。

4. 压缩
- 当累计达到 10 条时：
  - 生成 summary-10 的 Stateful 摘要（Current State / Decisions Kept / Open TODO / Carry Forward）。
  - 重置下一窗口计数（例如 W-0002）。

## 结构化记录模板（recent-5）
- 日期:
- 清洗后需求:
- 代码变更:
- 技术决策:
- 待办:

## 清洗规则（简版）
- 移除: “很高兴为你服务”“我来帮你”“好的收到”等寒暄语。
- 合并重复指令，保留最强约束版本。
- 句子改写为动作导向：动词 + 对象 + 约束。

## 质量检查
- 每条必须有 代码变更 / 技术决策 / 待办 三字段。
- context 不写长过程，只写结论与索引。
- 10 条摘要必须包含“可延续状态”（Carry Forward）。
- 需求澄清未完成时，不要写入伪设计或猜测性结论。

## 自动化脚本（PowerShell）
- 脚本路径：`.github/prompts/scripts/update-dialog-log.ps1`
- 功能：追加一条记录 -> 自动更新 daily -> 自动裁剪 recent-5 -> 自动刷新 summary-10（满 10 条时自动滚动窗口）。

### 示例命令
```powershell
powershell -ExecutionPolicy Bypass -File .github/prompts/scripts/update-dialog-log.ps1 `
  -Title "日志工作流自动化" `
  -Request "请追加一条日志，清洗开场白并提取变更/决策/待办" `
  -Changes "scripts/update-dialog-log.ps1", ".github/prompts/workflow-log.md" `
  -Decisions "日志追加流程由脚本统一执行" `
  -Todos "补齐第10条后校验窗口滚动行为"
```

### 预演模式（不落盘）
```powershell
powershell -ExecutionPolicy Bypass -File .github/prompts/scripts/update-dialog-log.ps1 `
  -Title "预演" `
  -Request "很高兴为你服务，请先做预演" `
  -DryRun
```
