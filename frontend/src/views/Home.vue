<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <a-layout-header class="home-header">
      <div class="header-left">
        <h2 class="header-title">AI RAG Demo</h2>
      </div>
      <div class="header-right">
        <a-space>
          <a-avatar :size="32" style="background-color: #165dff">
            {{ authStore.username.charAt(0).toUpperCase() }}
          </a-avatar>
          <span class="header-username">{{ authStore.username }}</span>
          <a-button type="text" @click="handleLogout">
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
              <a-tag v-if="docId" :color="statusColor">{{ statusText }}</a-tag>
            </template>

            <a-textarea
              v-model="content"
              placeholder="在此粘贴知识内容，让 AI 学习..."
              :auto-size="{ minRows: 6, maxRows: 12 }"
              :style="{ marginBottom: '16px' }"
            />

            <a-space>
              <a-button
                type="primary"
                :loading="uploading"
                @click="handleUpload"
                :disabled="!content.trim()"
              >
                <template #icon><icon-upload /></template>
                {{ uploading ? '上传中...' : '提交到知识库' }}
              </a-button>
              <a-button
                v-if="docId && status !== 'SUCCESS' && status !== 'FAILED'"
                @click="handleCheckStatus"
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
                  <a-typography-text copyable>{{ docId }}</a-typography-text>
                </a-descriptions-item>
                <a-descriptions-item label="处理状态">
                  <a-tag :color="statusColor">{{ statusText }}</a-tag>
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
                  >
                    <template #suffix>
                      <a-button
                        type="primary"
                        size="small"
                        :loading="asking"
                        @click="handleAsk"
                        :disabled="!question.trim()"
                      >
                        <template #icon><icon-send /></template>
                      </a-button>
                    </template>
                  </a-input>
                </div>
              </template>

              <!-- 问答结果 -->
              <template v-if="answer">
                <a-divider />
                <div class="qa-result">
                  <a-typography-paragraph>
                    <template #title>
                      <a-space>
                        <icon-bulb />
                        <strong>回答：</strong>
                      </a-space>
                    </template>
                    {{ answer }}
                  </a-typography-paragraph>
                </div>
              </template>

              <!-- 空状态 -->
              <template v-if="!answer && !asking">
                <a-empty description="输入问题开始对话" />
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
  background: #f2f3f5;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  height: 56px;
  line-height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-username {
  font-size: 14px;
  color: #4e5969;
}

.home-content {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.content-card {
  margin-bottom: 24px;
  border-radius: 8px;
  background: #fff;
}

.status-card {
  margin-top: 16px;
  background: #f7f8fa;
  border-radius: 6px;
}

.qa-input-area {
  width: 100%;
}

.qa-result {
  padding: 16px 0;
}
</style>
