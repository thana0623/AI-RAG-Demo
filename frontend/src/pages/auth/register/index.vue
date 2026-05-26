<template>
  <div class="auth-header">
    <h2>创建账号</h2>
    <p>注册后即可体验企业级知识问答</p>
  </div>

  <form class="auth-form" @submit.prevent="handleRegister">
    <BaseInput v-model="form.email" label="邮箱" placeholder="请输入邮箱" />
    <BaseInput v-model="form.username" label="用户名" placeholder="请输入用户名" />
    <BaseInput v-model="form.password" label="密码" type="password" placeholder="至少 6 位" />

    <div class="code-row">
      <BaseInput v-model="form.code" label="验证码" placeholder="6 位验证码" />
      <BaseButton
        class="code-button"
        variant="outline"
        type="button"
        :disabled="sendingCode || countdown > 0 || !form.email"
        @click="handleSendCode"
      >
        {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
      </BaseButton>
    </div>

    <BaseButton type="submit" :loading="loading" block>注册</BaseButton>
  </form>

  <p v-if="notice" class="notice">{{ notice }}</p>

  <div class="auth-links">
    <span>已有账号？</span>
    <RouterLink to="/login">去登录</RouterLink>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { sendCode, register } from '@/services/auth'
import { useRequest } from '@/hooks/useRequest'

const router = useRouter()

const form = reactive({
  email: '',
  username: '',
  password: '',
  code: ''
})

const sendingCode = ref(false)
const notice = ref('')
const countdown = ref(0)
let timer: number | null = null

const { loading, run } = useRequest(async () => {
  await register({
    email: form.email,
    username: form.username,
    password: form.password,
    code: form.code
  })
  router.push('/login')
})

const handleRegister = () => {
  notice.value = ''
  run().catch(() => {
    notice.value = '注册失败，请检查输入信息。'
  })
}

const handleSendCode = async () => {
  sendingCode.value = true
  try {
    await sendCode(form.email, 'REGISTER')
    countdown.value = 60
    timer = window.setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0 && timer) {
        window.clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (error) {
    notice.value = '验证码发送失败，请稍后再试。'
  } finally {
    sendingCode.value = false
  }
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

.code-row {
  display: grid;
  gap: 12px;
}

.code-button {
  height: 44px;
}

.auth-links {
  margin-top: var(--spacing-2);
  font-size: 12px;
  color: var(--color-text-muted);
  display: flex;
  justify-content: center;
  gap: 6px;
}

.auth-links a:hover {
  color: var(--color-primary);
}

.notice {
  margin-top: var(--spacing-2);
  font-size: 12px;
  color: #d14343;
}

@media (min-width: 520px) {
  .code-row {
    grid-template-columns: 1fr 140px;
    align-items: end;
  }
}
</style>
