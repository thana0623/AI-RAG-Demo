<template>
  <div class="status-card">
    <div>
      <p class="label">文档 ID</p>
      <div class="doc-row">
        <span class="doc-id">{{ docId }}</span>
        <button class="copy-btn" @click="copyId">复制</button>
      </div>
    </div>
    <div class="status-pill">
      <BaseTag :tone="tone">{{ statusText }}</BaseTag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseTag from '@/components/ui/BaseTag.vue'
import { DOC_STATUS_LABELS } from '@/constants'

const props = defineProps<{
  docId: string
  status: string
  tone: 'neutral' | 'info' | 'success' | 'warning' | 'danger'
}>()

const statusText = computed(() => DOC_STATUS_LABELS[props.status] || props.status)

const copyId = async () => {
  await navigator.clipboard.writeText(props.docId)
}
</script>

<style scoped>
.status-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.03);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.doc-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.doc-id {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 12px;
}

.copy-btn {
  border: none;
  background: transparent;
  color: var(--color-primary);
  font-size: 12px;
  cursor: pointer;
}
</style>
