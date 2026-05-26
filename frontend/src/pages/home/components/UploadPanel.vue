<template>
  <BaseCard title="1. 上传知识内容" subtitle="提交文档后异步向量化，状态由系统追踪">
    <template #actions>
      <BaseTag v-if="docId" :tone="tone">{{ statusText }}</BaseTag>
    </template>

    <div class="stack">
      <BaseTextarea
        v-model="content"
        :rows="8"
        placeholder="在此粘贴知识内容，帮助 AI 建立专属语义库"
      />

      <div class="actions">
        <BaseButton :loading="uploading" :disabled="!content.trim()" @click="handleUpload">
          {{ uploading ? '上传中...' : '提交到知识库' }}
        </BaseButton>
        <BaseButton
          v-if="docId && !['SUCCESS', 'FAILED'].includes(status)"
          variant="ghost"
          @click="handleCheckStatus"
        >
          刷新状态
        </BaseButton>
      </div>

      <DocumentStatusCard v-if="docId" :doc-id="docId" :status="status" :tone="tone" />
      <p v-if="notice" class="notice">{{ notice }}</p>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseTextarea from '@/components/ui/BaseTextarea.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseTag from '@/components/ui/BaseTag.vue'
import DocumentStatusCard from '@/components/business/DocumentStatusCard.vue'
import { DOC_STATUS_LABELS } from '@/constants'
import { uploadDocument, getDocumentStatus } from '@/services/rag'

const content = ref('')
const docId = ref('')
const status = ref('')
const uploading = ref(false)
const notice = ref('')

const tone = computed(() => {
  switch (status.value) {
    case 'SUCCESS':
      return 'success'
    case 'FAILED':
      return 'danger'
    case 'PROCESSING':
      return 'info'
    case 'PENDING':
      return 'warning'
    default:
      return 'neutral'
  }
})

const statusText = computed(() => DOC_STATUS_LABELS[status.value] || status.value || '待提交')

const handleUpload = async () => {
  uploading.value = true
  try {
    const res = await uploadDocument(content.value)
    docId.value = res.data.docId
    status.value = 'PENDING'
    notice.value = '已提交，正在排队向量化处理。'
  } catch (error) {
    notice.value = '上传失败，请检查网络或服务状态。'
  } finally {
    uploading.value = false
  }
}

const handleCheckStatus = async () => {
  if (!docId.value) return
  try {
    const res = await getDocumentStatus(docId.value)
    status.value = res.data.status
  } catch (error) {
    notice.value = '状态查询失败，请稍后再试。'
  }
}
</script>

<style scoped>
.stack {
  display: grid;
  gap: var(--spacing-2);
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.notice {
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
