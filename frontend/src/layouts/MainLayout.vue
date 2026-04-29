<template>
  <div class="app-shell">
    <div class="bg-grid"></div>
    <header class="topbar content-max">
      <div class="brand">
        <div class="brand-icon"></div>
        <div>
          <h1>AI RAG Demo</h1>
          <p>企业级知识问答控制台</p>
        </div>
      </div>
      <div class="top-actions">
        <UserBadge :username="username" :email="email" />
        <BaseButton variant="outline" @click="handleLogout">退出</BaseButton>
      </div>
    </header>
    <main class="content-max main-area">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUser } from '@/hooks/useUser'
import BaseButton from '@/components/ui/BaseButton.vue'
import UserBadge from '@/components/business/UserBadge.vue'

const router = useRouter()
const { store, username, email } = useUser()

const handleLogout = () => {
  store.signOut()
  router.push('/login')
}
</script>

<style scoped>
.topbar {
  position: relative;
  z-index: 1;
  padding: var(--spacing-4) var(--spacing-2);
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-3);
  align-items: center;
}

.brand {
  display: flex;
  gap: 14px;
  align-items: center;
}

.brand-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.18), rgba(22, 119, 255, 0.06));
  border: 1px solid rgba(22, 119, 255, 0.2);
}

.brand h1 {
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-title);
}

.brand p {
  font-size: 12px;
  color: var(--color-text-muted);
}

.top-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.main-area {
  padding: 0 var(--spacing-2) var(--spacing-5);
  position: relative;
  z-index: 1;
}

@media (max-width: 768px) {
  .topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .top-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
