<template>
  <div class="home-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
    </div>

    <!-- 顶部导航栏 -->
    <a-layout-header class="home-header">
      <div class="header-left">
        <svg viewBox="0 0 48 48" fill="none" width="28" height="28" style="margin-right: 10px;">
          <circle cx="24" cy="24" r="22" stroke="url(#hgrad)" stroke-width="2.5" fill="rgba(64,128,255,0.15)"/>
          <path d="M16 28L24 18L32 28" stroke="url(#hgrad)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M20 24L24 20L28 24" stroke="url(#hgrad)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <defs>
            <linearGradient id="hgrad" x1="12" y1="12" x2="36" y2="36">
              <stop stop-color="#4080FF"/>
              <stop offset="1" stop-color="#A855F7"/>
            </linearGradient>
          </defs>
        </svg>
        <h2 class="header-title">AI RAG Demo</h2>
      </div>
      <div class="header-right">
        <a-space>
          <a-avatar :size="32" class="header-avatar">
            {{ authStore.username.charAt(0).toUpperCase() }}
          </a-avatar>
          <span class="header-username">{{ authStore.username }}</span>
          <a-button type="text" @click="handleLogout" class="logout-btn">
            <template #icon><icon-export /></template>
            退出
          </a-button>
        </a-space>
      </div>
    </a-layout-header>

    <!-- 主内容区 -->
    <a-layout-content class="home-content">
      <a-row :gutter="24">
        <!-- 左侧：文档上传 -->
        <a-col :span="12">
          <a-card class="content-card" title="1. 上传知识内容" :bordered="false">
            <template #extra>
              <a-tag v-if="docId" :color="statusColor" class="status-tag">{{ statusText }}</a-tag>
            </template>

            <a-textarea
              v-model="content"
              placeholder="在此粘贴知识内容，让 AI 学习..."
              :auto-size="{ minRows: 6, maxRows: 12 }"
              class="custom-textarea"
            />

            <a-space style="margin-top: 16px;">
              <a-button
                type="primary"
                :loading="uploading"
                @click="handleUpload"
                :disabled="!content.trim()"
                class="action-btn"
              >
                <template #icon><icon-upload /></template>
                {{ uploading ? '上传中...' : '提交到知识库' }}
              </a-button>
              <a-button
                v-if="docId && status !== 'SUCCESS' && status !== 'FAILED'"
                @click="handleCheckStatus"
                class="ghost-btn"
              >
                刷新状态
              </a-button>
            </a-space>

            <!-- 文档状态信息 -->
            <a-card
              v-if="docId"
              class="status-card"
              :bordered="false"
            >
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="文档 ID">
                  <a-typography-text copyable class="mono-text">{{ docId }}</a-typography-text>
                </a-descriptions-item>
                <a-descriptions-item label="处理状态">
                  <a-tag :color="statusColor" class="status-tag">{{ statusText }}</a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-card>
        </a-col>

        <!-- 右侧：RAG 问答 -->
        <a-col :span="12">
          <a-card class="content-card" title="2. AI 知识问答" :bordered="false">
            <a-comment>
              <template #actions>
                <div class="qa-input-area">
                  <a-input
                    v-model="question"
                    placeholder="输入你的问题，按 Enter 发送..."
                    size="large"
                    @press-enter="handleAsk"
                    :disabled="asking"
                    class="qa-input"
                  >
                    <template #suffix>
                      <a-button
                        type="primary"
                        size="small"
                        :loading="asking"
                        @click="handleAsk"
                        :disabled="!question.trim()"
                        class="send-btn"
                      >
                        <template #icon><icon-send /></template>
                      </a-button>
                    </template>
                  </a-input>
                </div>
              </template>

              <!-- 问答结果 -->
              <template v-if="answer">
                <a-divider class="qa-divider" />
                <div class="qa-result">
                  <div class="qa-result-header">
                    <icon-bulb class="qa-icon" />
                    <strong>回答：</strong>
                  </div>
                  <a-typography-paragraph class="qa-text">
                    {{ answer }}
                  </a-typography-paragraph>
                </div>
              </template>

              <!-- 空状态 -->
              <template v-if="!answer && !asking">
                <div class="qa-empty">
                  <icon-bulb class="qa-empty-icon" />
                  <p>输入问题开始对话</p>
                </div>
              </template>

              <!-- 加载中 -->
              <template v-if="asking">
                <a-skeleton :loading="true" :animation="true">
                  <a-skeleton-line :rows="3" />
                </a-skeleton>
              </template>
            </a-comment>
          </a-card>
        </a-col>
      </a-row>
    </a-layout-content>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { uploadDocument, getDocumentStatus, askQuestion } from '../api/rag'
import { Message } from '@arco-design/web-vue'
import {
  IconUpload,
  IconSend,
  IconExport,
  IconBulb,
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const authStore = useAuthStore()

// 文档上传
const content = ref('')
const docId = ref(null)
const status = ref('')
const uploading = ref(false)

const statusMap = {
  PENDING: { color: 'orange', text: '排队中' },
  PROCESSING: { color: 'blue', text: '处理中' },
  SUCCESS: { color: 'green', text: '已完成' },
  FAILED: { color: 'red', text: '失败' },
  NOT_FOUND: { color: 'gray', text: '未找到' },
}

const statusColor = computed(() => statusMap[status.value]?.color || 'gray')
const statusText = computed(() => statusMap[status.value]?.text || status.value || '-')

async function handleUpload() {
  if (!content.value.trim()) {
    Message.warning('请输入知识内容')
    return
  }
  uploading.value = true
  try {
    const res = await uploadDocument(content.value)
    docId.value = res.data.docId
    status.value = 'PENDING'
    Message.success('文档已提交，正在异步处理...')
  } catch (e) {
    Message.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleCheckStatus() {
  if (!docId.value) return
  try {
    const res = await getDocumentStatus(docId.value)
    status.value = res.data.status
  } catch (e) {
    Message.error(e.message || '查询状态失败')
  }
}

// RAG 问答
const question = ref('')
const answer = ref('')
const asking = ref(false)

async function handleAsk() {
  if (!question.value.trim()) {
    Message.warning('请输入问题')
    return
  }
  asking.value = true
  answer.value = ''
  try {
    const res = await askQuestion(question.value)
    answer.value = res.data.answer
  } catch (e) {
    Message.error(e.message || '问答请求失败')
  } finally {
    asking.value = false
  }
}

// 退出登录
function handleLogout() {
  authStore.logout()
  Message.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29 0%, #1a1a4e 50%, #24243e 100%);
  position: relative;
}

/* 背景装饰 */
.bg-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.3;
}

.bg-circle-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #4080FF, #6366f1);
  top: -200px;
  right: -100px;
  animation: float 10s ease-in-out infinite;
}

.bg-circle-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #A855F7, #ec4899);
  bottom: -150px;
  left: -100px;
  animation: float 12s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

/* 顶部导航 */
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  height: 60px;
  line-height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-avatar {
  background: linear-gradient(135deg, #4080FF, #A855F7) !important;
  font-weight: 600;
  font-size: 14px;
}

.header-username {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.logout-btn {
  color: rgba(255, 255, 255, 0.5) !important;
  transition: color 0.3s ease !important;
}

.logout-btn:hover {
  color: #f87171 !important;
  background: rgba(248, 113, 113, 0.1) !important;
}

/* 主内容 */
.home-content {
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.content-card {
  margin-bottom: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.content-card:hover {
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

:deep(.arco-card-header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.06) !important;
  padding: 18px 20px !important;
}

:deep(.arco-card-header-title) {
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: 16px !important;
  font-weight: 600 !important;
}

:deep(.arco-card-body) {
  padding: 20px !important;
}

/* 自定义文本域 */
.custom-textarea {
  background: rgba(255, 255, 255, 0.04) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: rgba(255, 255, 255, 0.85) !important;
  border-radius: 12px !important;
  transition: all 0.3s ease;
}

.custom-textarea:hover {
  border-color: rgba(96, 165, 250, 0.3) !important;
  background: rgba(255, 255, 255, 0.06) !important;
}

.custom-textarea:focus {
  border-color: #60a5fa !important;
  background: rgba(255, 255, 255, 0.08) !important;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1) !important;
}

.custom-textarea::placeholder {
  color: rgba(255, 255, 255, 0.25) !important;
}

/* 按钮 */
.action-btn {
  height: 40px !important;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #4080FF, #A855F7) !important;
  border: none !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(64, 128, 255, 0.35) !important;
}

.ghost-btn {
  height: 40px !important;
  border-radius: 10px !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
  background: rgba(255, 255, 255, 0.04) !important;
  color: rgba(255, 255, 255, 0.6) !important;
  transition: all 0.3s ease !important;
}

.ghost-btn:hover {
  border-color: rgba(96, 165, 250, 0.4) !important;
  color: #60a5fa !important;
  background: rgba(96, 165, 250, 0.1) !important;
}

/* 状态卡片 */
.status-card {
  margin-top: 16px;
  background: rgba(255, 255, 255, 0.03) !important;
  border-radius: 10px !important;
  border: 1px solid rgba(255, 255, 255, 0.06) !important;
}

:deep(.arco-descriptions-item-label) {
  color: rgba(255, 255, 255, 0.5) !important;
}

:deep(.arco-descriptions-item-value) {
  color: rgba(255, 255, 255, 0.8) !important;
}

.mono-text {
  color: rgba(255, 255, 255, 0.7) !important;
  font-family: 'SF Mono', 'Fira Code', monospace !important;
  font-size: 12px !important;
}

.status-tag {
  border-radius: 6px !important;
  font-size: 12px !important;
  padding: 2px 10px !important;
}

/* 问答区域 */
.qa-input-area {
  width: 100%;
}

.qa-input {
  background: rgba(255, 255, 255, 0.04) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 12px !important;
  transition: all 0.3s ease;
}

.qa-input:hover {
  border-color: rgba(96, 165, 250, 0.3) !important;
  background: rgba(255, 255, 255, 0.06) !important;
}

:deep(.qa-input.arco-input-focus) {
  border-color: #60a5fa !important;
  background: rgba(255, 255, 255, 0.08) !important;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1) !important;
}

:deep(.qa-input .arco-input) {
  color: rgba(255, 255, 255, 0.85) !important;
}

:deep(.qa-input .arco-input::placeholder) {
  color: rgba(255, 255, 255, 0.25) !important;
}

.send-btn {
  border-radius: 8px !important;
  background: linear-gradient(135deg, #4080FF, #A855F7) !important;
  border: none !important;
  transition: all 0.3s ease !important;
}

.send-btn:hover:not(:disabled) {
  box-shadow: 0 2px 12px rgba(64, 128, 255, 0.4) !important;
}

.qa-divider {
  border-color: rgba(255, 255, 255, 0.08) !important;
  margin: 16px 0 !important;
}

.qa-result {
  padding: 8px 0;
}

.qa-result-header {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
  font-size: 14px;
}

.qa-icon {
  color: #60a5fa;
  font-size: 18px;
}

.qa-text {
  color: rgba(255, 255, 255, 0.75) !important;
  line-height: 1.8 !important;
  font-size: 14px !important;
}

.qa-empty {
  text-align: center;
  padding: 40px 0;
  color: rgba(255, 255, 255, 0.3);
}

.qa-empty-icon {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.15);
  margin-bottom: 12px;
}

.qa-empty p {
  font-size: 14px;
  margin: 0;
}

/* 覆盖 Arco 骨架屏颜色 */
:deep(.arco-skeleton-line) {
  background: rgba(255, 255, 255, 0.05) !important;
}
</style>
