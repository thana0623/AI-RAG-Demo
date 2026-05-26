<template>
  <div class="skeleton" :style="{ height: `${height}px` }"></div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    height?: number
  }>(),
  {
    height: 12
  }
)
</script>

<style scoped>
.skeleton {
  width: 100%;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(15, 23, 42, 0.05), rgba(15, 23, 42, 0.12), rgba(15, 23, 42, 0.05));
  background-size: 200% 100%;
  animation: shimmer 1.4s ease infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
