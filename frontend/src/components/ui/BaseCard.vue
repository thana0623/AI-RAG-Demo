<template>
  <section class="card">
    <header v-if="title || $slots.header" class="card-header">
      <slot name="header">
        <h3 class="card-title">{{ title }}</h3>
        <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
      </slot>
      <div v-if="$slots.actions" class="card-actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="card-body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  subtitle?: string
}>()
</script>

<style scoped>
.card {
  background: var(--color-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  border: 1px solid rgba(15, 23, 42, 0.04);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--spacing-3);
  border-bottom: 1px solid rgba(15, 23, 42, 0.05);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.card-subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.card-actions {
  margin-left: auto;
}

.card-body {
  padding: var(--spacing-3);
}
</style>
