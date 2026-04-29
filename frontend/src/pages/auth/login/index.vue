<template>
  <div class="auth-header">
    <h2>欢迎回来</h2>
    <p>登录后继续使用企业级 RAG 问答能力</p>
  </div>

  <form class="auth-form" @submit.prevent="handleLogin">
    <BaseInput v-model="form.identifier" label="邮箱 / 用户名" placeholder="请输入邮箱或用户名" />
    <BaseInput v-model="form.password" label="密码" type="password" placeholder="请输入密码" />

    <BaseButton type="submit" :loading="loading" block>登录</BaseButton>
  </form>

  <p v-if="notice" class="notice">{{ notice }}</p>

  <div class="auth-links">
    <RouterLink to="/register">注册账号</RouterLink>
    <RouterLink to="/forgot-password">忘记密码</RouterLink>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { useUserStore } from '@/store/modules/user'
import { useRequest } from '@/hooks/useRequest'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  identifier: '',
  password: ''
})

const notice = ref('')

const { loading, run } = useRequest(async () => {
  await userStore.signIn(form.identifier, form.password)
  const redirect = (route.query.redirect as string) || '/'
  router.push(redirect)
})

const handleLogin = () => {
  notice.value = ''
  run().catch(() => {
    notice.value = '登录失败，请检查账号或密码。'
  })
}
</script>

<style scoped>
.auth-header {
  display: grid;
  gap: 6px;
  margin-bottom: var(--spacing-3);
}

.auth-header h2 {
  font-size: 22px;
  font-weight: 700;
}

.auth-header p {
  font-size: 13px;
  color: var(--color-text-muted);
}

.auth-form {
  display: grid;
  gap: var(--spacing-2);
}

.auth-links {
  display: flex;
  justify-content: space-between;
  margin-top: var(--spacing-2);
  font-size: 12px;
  color: var(--color-text-muted);
}

.auth-links a:hover {
  color: var(--color-primary);
}

.notice {
  margin-top: var(--spacing-2);
  font-size: 12px;
  color: #d14343;
}
</style>
