<template>
  <label class="field">
    <span v-if="label" class="label">{{ label }}</span>
    <textarea
      :value="modelValue"
      :placeholder="placeholder"
      :rows="rows"
      :disabled="disabled"
      @input="$emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
    ></textarea>
  </label>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: string
  label?: string
  placeholder?: string
  rows?: number
  disabled?: boolean
}>()

defineEmits(['update:modelValue'])
</script>

<style scoped>
.field {
  display: grid;
  gap: 8px;
  width: 100%;
}

.label {
  font-size: 12px;
  color: var(--color-text-muted);
}

textarea {
  width: 100%;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(15, 23, 42, 0.12);
  padding: 12px 14px;
  font-size: 14px;
  background: #fff;
  resize: vertical;
  transition: border 0.2s ease, box-shadow 0.2s ease;
}

textarea:focus {
  outline: none;
  border-color: rgba(22, 119, 255, 0.4);
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.12);
}

textarea:disabled {
  background: rgba(15, 23, 42, 0.05);
}
</style>
