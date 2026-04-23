<template>
  <div class="login-wrapper">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48" fill="none" width="48" height="48">
            <circle cx="24" cy="24" r="22" stroke="url(#grad1)" stroke-width="2.5" fill="rgba(64,128,255,0.1)"/>
            <path d="M16 28L24 18L32 28" stroke="url(#grad1)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M20 24L24 20L28 24" stroke="url(#grad1)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="grad1" x1="12" y1="12" x2="36" y2="36">
                <stop stop-color="#4080FF"/>
                <stop offset="1" stop-color="#A855F7"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="login-title">AI RAG Demo</h1>
        <p class="login-subtitle">智能知识库 · 让 AI 理解你的数据</p>
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
            class="login-btn"
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
  background: linear-gradient(135deg, #0f0c29 0%, #1a1a4e 30%, #24243e 60%, #0f0c29 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰圆 */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.bg-circle-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #4080FF, #6366f1);
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.bg-circle-2 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, #A855F7, #ec4899);
  bottom: -80px;
  left: -80px;
  animation: float 10s ease-in-out infinite reverse;
}

.bg-circle-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #06b6d4, #3b82f6);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float 12s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.login-card {
  width: 420px;
  padding: 48px 40px 40px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 48px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.5px;
}

.login-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 8px;
  letter-spacing: 0.3px;
}

/* 覆盖 Arco Design 表单样式 */
:deep(.arco-form-item-label) {
  color: rgba(255, 255, 255, 0.7) !important;
  font-size: 13px;
  font-weight: 500;
}

:deep(.arco-input) {
  background: rgba(255, 255, 255, 0.06) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
  border-radius: 12px !important;
  transition: all 0.3s ease;
}

:deep(.arco-input:hover) {
  border-color: rgba(96, 165, 250, 0.4) !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

:deep(.arco-input:focus) {
  border-color: #60a5fa !important;
  background: rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.15) !important;
}

:deep(.arco-input::placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.arco-input-prefix) {
  color: rgba(255, 255, 255, 0.4) !important;
}

:deep(.arco-input-password) {
  background: rgba(255, 255, 255, 0.06) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 12px !important;
}

:deep(.arco-input-password:hover) {
  border-color: rgba(96, 165, 250, 0.4) !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

:deep(.arco-input-password.arco-input-focus) {
  border-color: #60a5fa !important;
  background: rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.15) !important;
}

:deep(.arco-input-password .arco-input) {
  background: transparent !important;
  border: none !important;
}

:deep(.arco-input-password .arco-input-prefix) {
  color: rgba(255, 255, 255, 0.4) !important;
}

:deep(.arco-input-password .arco-input-suffix) {
  color: rgba(255, 255, 255, 0.4) !important;
}

.login-btn {
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #4080FF, #A855F7) !important;
  border: none !important;
  transition: all 0.3s ease !important;
  letter-spacing: 2px;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(64, 128, 255, 0.4) !important;
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

:deep(.arco-link) {
  color: rgba(255, 255, 255, 0.5) !important;
  font-size: 13px;
  transition: color 0.3s ease;
}

:deep(.arco-link:hover) {
  color: #60a5fa !important;
}
</style>
