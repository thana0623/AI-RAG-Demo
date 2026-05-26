<template>
  <div class="app-shell">
    <div class="bg-grid"></div>
    <main class="auth-wrapper">
      <div class="auth-card">
        <slot />
      </div>
    </main>
  </div>
</template>

<style scoped>
.auth-wrapper {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: var(--spacing-4) var(--spacing-2);
}

.auth-card {
  width: min(420px, 100%);
  background: var(--color-card);
  border-radius: 20px;
  box-shadow: var(--shadow-sm);
  padding: var(--spacing-4);
  border: 1px solid rgba(15, 23, 42, 0.05);
}
</style>
