<template>
  <section class="hero stagger">
    <div>
      <h2>知识接入与问答</h2>
      <p>上传内容，生成向量化语义层，快速驱动企业级检索问答。</p>
    </div>
    <div class="hero-actions">
      <div class="stat">
        <span>运行模式</span>
        <strong>本地开发</strong>
      </div>
      <div class="stat">
        <span>数据源</span>
        <strong>MySQL + Redis</strong>
      </div>
    </div>
  </section>

  <section class="grid">
    <UploadPanel class="stagger" />
    <AskPanel class="stagger" />
  </section>
</template>

<script setup lang="ts">
import UploadPanel from './components/UploadPanel.vue'
import AskPanel from './components/AskPanel.vue'
</script>

<style scoped>
.hero {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-3);
  align-items: center;
  padding: var(--spacing-3);
  background: #ffffff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  margin-bottom: var(--spacing-3);
}

.hero h2 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 6px;
}

.hero p {
  font-size: 13px;
  color: var(--color-text-muted);
}

.hero-actions {
  display: flex;
  gap: var(--spacing-2);
}

.stat {
  display: grid;
  gap: 4px;
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  background: rgba(22, 119, 255, 0.08);
  font-size: 12px;
}

.stat strong {
  font-size: 14px;
  color: var(--color-primary);
}

.grid {
  display: grid;
  gap: var(--spacing-3);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
