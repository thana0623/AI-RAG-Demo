<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">AI RAG Demo</h1>
        <p class="login-subtitle">登录以继续使用</p>
      </div>

      <a-form
        :model="form"
        :rules="rules"
        @submit="handleLogin"
        layout="vertical"
        auto-label-width
      >
        <a-form-item field="identifier" label="邮箱 / 用户名">
          <a-input
            v-model="form.identifier"
            placeholder="请输入邮箱或用户名"
            size="large"
          >
            <template #prefix>
              <icon-user />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item field="password" label="密码">
          <a-input-password
            v-model="form.password"
            placeholder="请输入密码"
            size="large"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            long
            size="large"
            :loading="loading"
          >
            登 录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
        <a-link @click="$router.push('/register')">注册账号</a-link>
        <a-link @click="$router.push('/forgot-password')">忘记密码？</a-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { Message } from '@arco-design/web-vue'
import { IconUser, IconLock } from '@arco-design/web-vue/es/icon'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = reactive({
  identifier: '',
  password: '',
})

const rules = {
  identifier: [
    { required: true, message: '请输入邮箱或用户名' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, message: '密码至少 6 位' },
  ],
}

const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(form.identifier, form.password)
    Message.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    Message.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  margin: 0;
}

.login-subtitle {
  font-size: 14px;
  color: #86909c;
  margin-top: 8px;
}

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
}
</style>
