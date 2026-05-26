<template>
  <span :class="['tag', tone]">
    <slot />
  </span>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    tone?: 'neutral' | 'info' | 'success' | 'warning' | 'danger'
  }>(),
  {
    tone: 'neutral'
  }
)
</script>

<style scoped>
.tag {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(15, 23, 42, 0.06);
  color: var(--color-text);
}

.tag.info {
  background: rgba(22, 119, 255, 0.12);
  color: var(--color-primary);
}

.tag.success {
  background: rgba(34, 197, 94, 0.12);
  color: #16a34a;
}

.tag.warning {
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
}

.tag.danger {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}
</style>
