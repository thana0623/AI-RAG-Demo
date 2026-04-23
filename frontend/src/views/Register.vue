<template>
  <div class="register-wrapper">
    <div class="register-card">
      <div class="register-header">
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
    Message.success('验证码已发送')
    // 开始倒计时
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 440px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  margin: 0;
}

.register-subtitle {
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

.register-footer {
  text-align: center;
  margin-top: 16px;
}
</style>
