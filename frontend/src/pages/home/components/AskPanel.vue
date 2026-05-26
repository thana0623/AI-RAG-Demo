<template>
  <BaseCard title="2. AI 知识问答" subtitle="检索知识库，返回可追溯答案">
    <div class="stack">
      <BaseInput
        v-model="question"
        label="问题"
        placeholder="输入你的问题"
        @keydown.enter.prevent="handleAsk"
      />
      <BaseButton :loading="asking" :disabled="!question.trim()" @click="handleAsk">
        发送问题
      </BaseButton>

      <div v-if="asking" class="skeleton">
        <BaseSkeleton :height="14" />
        <BaseSkeleton :height="14" />
        <BaseSkeleton :height="14" />
      </div>

      <div v-else-if="answer" class="answer">
        <p class="answer-title">回答</p>
        <p class="answer-body">{{ answer }}</p>
      </div>

      <div v-else class="empty">
        <p>输入问题开始检索</p>
      </div>

      <p v-if="notice" class="notice">{{ notice }}</p>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseSkeleton from '@/components/ui/BaseSkeleton.vue'
import { askQuestion } from '@/services/rag'

const question = ref('')
const answer = ref('')
const asking = ref(false)
const notice = ref('')

const handleAsk = async () => {
  asking.value = true
  answer.value = ''
  try {
    const res = await askQuestion(question.value)
    answer.value = res.data.answer
    notice.value = ''
  } catch (error) {
    notice.value = '问答请求失败，请稍后再试。'
  } finally {
    asking.value = false
  }
}
</script>

<style scoped>
.stack {
  display: grid;
  gap: var(--spacing-2);
}

.answer {
  padding: 12px;
  background: rgba(22, 119, 255, 0.08);
  border-radius: var(--radius-sm);
}

.answer-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 6px;
}

.answer-body {
  font-size: 14px;
  line-height: 1.6;
}

.empty {
  font-size: 13px;
  color: var(--color-text-muted);
  text-align: center;
  padding: 12px 0;
}

.skeleton {
  display: grid;
  gap: 8px;
}

.notice {
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
