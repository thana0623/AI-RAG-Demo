<template>
  <div class="container">
    <h1>AI RAG (MQ + Redis) Demo</h1>
    
    <div class="card">
      <h2>1. Upload Content to Train AI</h2>
      <textarea v-model="content" placeholder="Paste some knowledge for the AI here..."></textarea>
      <button @click="uploadDocument" :disabled="uploading">
        {{ uploading ? 'Uploading to MQ...' : 'Submit to Rag' }}
      </button>
      <div v-if="docId">
        <p>Doc ID: {{ docId }}</p>
        <p>Status (Redis): <strong>{{ status }}</strong></p>
        <button @click="checkStatus" v-if="status !== 'SUCCESS' && status !== 'FAILED'">Refresh Status</button>
      </div>
    </div>

    <div class="card">
      <h2>2. Chat with RAG</h2>
      <input v-model="question" placeholder="Ask a question..." @keyup.enter="askQuestion" />
      <button @click="askQuestion" :disabled="asking">
        {{ asking ? 'Thinking...' : 'Ask' }}
      </button>
      
      <div class="answer-box" v-if="answer">
        <strong>Answer:</strong>
        <p>{{ answer }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const content = ref('')
const docId = ref(null)
const status = ref('')
const uploading = ref(false)

const question = ref('')
const answer = ref('')
const asking = ref(false)

const uploadDocument = async () => {
  if (!content.value.trim()) return alert('Please enter knowledge content');
  uploading.value = true;
  try {
    const res = await fetch('/api/rag/document', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content: content.value })
    })
    const response = await res.json()
    const data = response.data
    docId.value = data.docId
    status.value = 'PENDING (in queue)'
  } finally {
    uploading.value = false;
  }
}

const checkStatus = async () => {
  if (!docId.value) return;
  const res = await fetch(`/api/rag/status/${docId.value}`)
  const response = await res.json()
  status.value = response.data.status
}

const askQuestion = async () => {
  if (!question.value.trim()) return;
  asking.value = true;
  answer.value = '';
  try {
    const res = await fetch('/api/rag/ask', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question: question.value })
    })
    const response = await res.json()
    if (response.code === 200) {
      answer.value = response.data.answer
    } else {
      answer.value = response.message
    }
  } finally {
    asking.value = false;
  }
}
</script>

<style>
.container { max-width: 800px; margin: 0 auto; font-family: sans-serif; padding: 20px; }
.card { border: 1px solid #ccc; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
textarea { width: 100%; height: 100px; margin-bottom: 10px; }
input { width: 100%; padding: 8px; margin-bottom: 10px; box-sizing: border-box;}
button { padding: 8px 16px; cursor: pointer; }
.answer-box { background-color: #f9f9f9; padding: 15px; margin-top: 15px; border-radius: 4px; }
</style>
