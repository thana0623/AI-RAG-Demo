<template>
  <button :class="['base-button', variant, { block }]" :type="type" :disabled="disabled || loading">
    <span v-if="loading" class="spinner" aria-hidden="true"></span>
    <slot />
  </button>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'primary' | 'ghost' | 'outline'
    type?: 'button' | 'submit'
    loading?: boolean
    disabled?: boolean
    block?: boolean
  }>(),
  {
    variant: 'primary',
    type: 'button',
    loading: false,
    disabled: false,
    block: false
  }
)
</script>

<style scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 18px;
  border-radius: var(--radius-md);
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.base-button.primary {
  background: var(--color-primary);
  color: #fff;
  box-shadow: var(--shadow-xs);
}

.base-button.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.base-button.outline {
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.12);
  color: var(--color-text);
}

.base-button.outline:hover:not(:disabled) {
  border-color: rgba(22, 119, 255, 0.4);
  color: var(--color-primary);
}

.base-button.ghost {
  background: rgba(22, 119, 255, 0.08);
  color: var(--color-primary);
}

.base-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.base-button.block {
  width: 100%;
}

.spinner {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-top-color: #fff;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
