# 近 10 条对话状态摘要（Stateful）

> 用途：每累计 10 条对话与操作后，输出一段有状态摘要，沉淀可延续上下文。

## 窗口元数据
- window_id: W-0001
- 统计范围: Entry-001 ~ Entry-010
- 当前已收录: 6 / 10
- 数据来源:
  - .github/prompts/recent-5.md
  - .github/prompts/daily/2026-04-23.md
  - .github/prompts/daily/2026-04-28.md

## Stateful 摘要（草稿）
### Current State
- 当前已建立可执行脚本：追加一条即可自动维护 recent-5、summary-10 与 daily。
- 现窗口已累计 6 条，下一阶段目标是累计到 10 条并验证窗口滚动。
- 前端已完成 TypeScript 化与自研 UI 体系重构，文档同步完成。

### Decisions Kept
- 使用脚本驱动的递进式日志维护流程。
- context 继续只保留索引与关键结论，细节写入 daily/recent/summary。

### Open TODO
- 补齐 Entry-007 ~ Entry-010。
- 达到 10 条后，复核自动生成摘要的质量并做模板微调。

### Carry Forward
- 后续每次对话都通过脚本追加，保持状态连续性与格式一致性。

## 10 条压缩模板（用于满 10 条时替换）
- Window: W-XXXX（Entry-AAA ~ Entry-BBB）
- Delta State: 本窗口相对上窗口的核心变化
- Stable Decisions: 仍有效且继续沿用的决策
- Invalidated Decisions: 已废弃或替换的决策
- Key File Changes: 本窗口关键文件变更集合
- Pending TODO: 延续到下一窗口的待办
- Next Actions: 下一个窗口第一优先级动作

