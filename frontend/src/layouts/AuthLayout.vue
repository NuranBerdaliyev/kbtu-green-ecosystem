<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="auth">
    <div class="auth__panel">
      <RouterLink :to="{ name: 'home' }" class="eyebrow brand">KBTU Green Ecosystem</RouterLink>
      <h2 class="auth__claim">Экологическая активность, которая работает на вашу карьеру.</h2>
      <p class="auth__note">
        Совместные поездки, сортировка отходов и участие в жизни кампуса формируют ваш ESG-профиль.
      </p>

      <!-- Always offer a way forward, so this panel is never a dead end. -->
      <nav class="auth__actions">
        <RouterLink :to="{ name: 'login' }" class="action action--solid">Войти</RouterLink>
        <RouterLink :to="{ name: 'register' }" class="action">Зарегистрироваться</RouterLink>
      </nav>
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

.brand {
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

.auth__actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-3);
  flex-wrap: wrap;
}

.action {
  padding: var(--space-3) var(--space-5);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  font-weight: 600;
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease;
}

.action:hover {
  border-color: #fff;
  background: rgba(255, 255, 255, 0.12);
}

.action--solid {
  background: #fff;
  border-color: #fff;
  color: var(--c-moss-dark);
}

.action--solid:hover {
  background: var(--c-moss-soft);
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
