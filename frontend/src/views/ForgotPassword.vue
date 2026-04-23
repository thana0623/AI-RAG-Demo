<template>
  <div class="forgot-wrapper">
    <div class="forgot-card">
      <div class="forgot-header">
        <h1 class="forgot-title">找回密码</h1>
        <p class="forgot-subtitle">通过邮箱验证码重置密码</p>
      </div>

      <a-form
        :model="form"
        :rules="rules"
        @submit="handleReset"
        layout="vertical"
      >
        <a-form-item field="email" label="邮箱">
          <a-input
            v-model="form.email"
            placeholder="请输入注册时使用的邮箱"
            size="large"
          >
            <template #prefix>
              <icon-email />
            </template>
          </a-input>
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

        <a-form-item field="newPassword" label="新密码">
          <a-input-password
            v-model="form.newPassword"
            placeholder="请输入新密码（至少 6 位）"
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
            重置密码
          </a-button>
        </a-form-item>
      </a-form>

      <div class="forgot-footer">
        <span>想起密码了？</span>
        <a-link @click="$router.push('/login')">去登录</a-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { sendCode, resetPassword } from '../api/auth'
import { Message } from '@arco-design/web-vue'
import { IconLock, IconEmail } from '@arco-design/web-vue/es/icon'

const router = useRouter()

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '邮箱格式不正确' },
  ],
  code: [
    { required: true, message: '请输入验证码' },
    { minLength: 6, maxLength: 6, message: '验证码为 6 位数字' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码' },
    { minLength: 6, message: '密码至少 6 位' },
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
    await sendCode(form.email, 'RESET')
    Message.success('验证码已发送')
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

async function handleReset() {
  loading.value = true
  try {
    await resetPassword({
      email: form.email,
      newPassword: form.newPassword,
      code: form.code,
    })
    Message.success('密码重置成功，请登录')
    router.push('/login')
  } catch (e) {
    Message.error(e.message || '重置密码失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.forgot-card {
  width: 440px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.forgot-header {
  text-align: center;
  margin-bottom: 32px;
}

.forgot-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  margin: 0;
}

.forgot-subtitle {
  font-size: 14px;
  color: #86909c;
  margin-top: 8px;
}

.code-row {
  display: flex;
  gap: 12px;
}

.code-btn {
  min-width: 120px;
  flex-shrink: 0;
}

.forgot-footer {
  text-align: center;
  margin-top: 16px;
}
</style>
