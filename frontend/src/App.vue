<template>
  <RouterView v-slot="{ Component, route }">
    <Transition name="fade" mode="out-in">
      <component :is="layoutFor(route.meta.layout)">
        <component :is="Component" />
      </component>
    </Transition>
  </RouterView>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import MobileLayout from '@/layouts/MobileLayout.vue'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const layoutFor = (layout?: unknown) => {
  if (layout === 'auth') return MobileLayout
  return MainLayout
}

onMounted(() => {
  userStore.init().catch(() => undefined)
})
</script>
