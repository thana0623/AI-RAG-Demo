<template>
  <div class="user-badge">
    <div class="avatar">{{ initial }}</div>
    <div class="info">
      <span class="name">{{ username || '未命名用户' }}</span>
      <span class="email">{{ email || 'no-email' }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  username: string
  email: string
}>()

const initial = computed(() => (props.username || 'U').charAt(0).toUpperCase())
</script>

<style scoped>
.user-badge {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: rgba(22, 119, 255, 0.12);
  color: var(--color-primary);
  display: grid;
  place-items: center;
  font-weight: 700;
}

.info {
  display: grid;
}

.name {
  font-size: 14px;
  font-weight: 600;
}

.email {
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
