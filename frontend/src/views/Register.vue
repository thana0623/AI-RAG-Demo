<template>
  <div class="register-wrapper">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <div class="register-card">
      <div class="register-header">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48" fill="none" width="40" height="40">
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
        <h1 class="register-title">创建账号</h1>
        <p class="register-subtitle">注册后即可使用 AI RAG 问答服务</p>
      </div>

      <a-form
        :model="form"
        :rules="rules"
        @submit="handleRegister"
        layout="vertical"
      >
        <a-form-item field="email" label="邮箱">
          <a-input
            v-model="form.email"
            placeholder="请输入邮箱"
            size="large"
          >
            <template #prefix>
              <icon-email />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item field="username" label="用户名">
          <a-input
            v-model="form.username"
            placeholder="请输入用户名"
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
            placeholder="请输入密码（至少 6 位）"
            size="large"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item field="code" label="验证码">
          <div class="code-row">
            <a-input
              v-model="form.code"
              placeholder="请输入验证码"
              size="large"
              style="flex: 1"
            />
            <a-button
              :disabled="sendingCode || countdown > 0"
              @click="handleSendCode"
              size="large"
              class="code-btn"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </a-button>
          </div>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            long
            size="large"
            :loading="loading"
            class="register-btn"
          >
            注 册
          </a-button>
        </a-form-item>
      </a-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <a-link @click="$router.push('/login')">去登录</a-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { sendCode, register } from '../api/auth'
import { Message } from '@arco-design/web-vue'
import { IconUser, IconLock, IconEmail } from '@arco-design/web-vue/es/icon'

const router = useRouter()

const form = reactive({
  email: '',
  username: '',
  password: '',
  code: '',
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '邮箱格式不正确' },
  ],
  username: [
    { required: true, message: '请输入用户名' },
    { minLength: 2, message: '用户名至少 2 位' },
    { maxLength: 20, message: '用户名最多 20 位' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, message: '密码至少 6 位' },
  ],
  code: [
    { required: true, message: '请输入验证码' },
    { minLength: 6, maxLength: 6, message: '验证码为 6 位数字' },
  ],
}

const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let timer = null

async function handleSendCode() {
  if (!form.email) {
    Message.warning('请先输入邮箱')
    return
  }
  sendingCode.value = true
  try {
    await sendCode(form.email, 'REGISTER')
    Message.success('验证码已发送（请查看后端日志）')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e) {
    Message.error(e.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    await register({
      email: form.email,
      username: form.username,
      password: form.password,
      code: form.code,
    })
    Message.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    Message.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29 0%, #1a1a4e 30%, #24243e 60%, #0f0c29 100%);
  position: relative;
  overflow: hidden;
}

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

.register-card {
  width: 440px;
  padding: 40px 40px 36px;
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

.register-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 48px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo-icon {
  margin-bottom: 12px;
  display: flex;
  justify-content: center;
}

.register-title {
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.register-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 6px;
}

.code-row {
  display: flex;
  gap: 12px;
}

.code-btn {
  min-width: 120px;
  flex-shrink: 0;
  height: 40px !important;
  border-radius: 10px !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
  background: rgba(255, 255, 255, 0.06) !important;
  color: rgba(255, 255, 255, 0.7) !important;
  transition: all 0.3s ease !important;
}

.code-btn:hover:not(:disabled) {
  border-color: rgba(96, 165, 250, 0.4) !important;
  background: rgba(255, 255, 255, 0.1) !important;
  color: #60a5fa !important;
}

.code-btn:disabled {
  opacity: 0.4 !important;
  cursor: not-allowed !important;
}

.register-btn {
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #4080FF, #A855F7) !important;
  border: none !important;
  transition: all 0.3s ease !important;
  letter-spacing: 2px;
}

.register-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(64, 128, 255, 0.4) !important;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
}

/* 复用登录页的表单样式 */
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

:deep(.arco-link) {
  color: rgba(255, 255, 255, 0.5) !important;
  font-size: 13px;
  transition: color 0.3s ease;
}

:deep(.arco-link:hover) {
  color: #60a5fa !important;
}
</style>
