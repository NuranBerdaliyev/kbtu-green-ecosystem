<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="auth">
    <div class="auth__panel">
      <p class="eyebrow">KBTU Green Ecosystem</p>
      <h2 class="auth__claim">Экологическая активность, которая работает на вашу карьеру.</h2>
      <p class="auth__note">
        Совместные поездки, сортировка отходов и участие в жизни кампуса формируют ваш ESG-профиль.
      </p>
    </div>
    <main class="auth__form">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.auth {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
}

.auth__panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-4);
  padding: var(--space-8);
  background: var(--c-moss);
  color: #fff;
}

.auth__panel .eyebrow {
  color: var(--c-moss-soft);
}

.auth__claim {
  font-size: var(--text-3xl);
  max-width: 12ch;
}

.auth__note {
  max-width: 40ch;
  color: var(--c-moss-soft);
}

.auth__form {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
}

@media (max-width: 860px) {
  .auth {
    grid-template-columns: 1fr;
  }

  .auth__panel {
    padding: var(--space-6);
  }

  .auth__claim {
    font-size: var(--text-xl);
    max-width: none;
  }
}
</style>
