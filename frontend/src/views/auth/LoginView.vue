<script setup>
import { reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ email: '', password: '' })

async function submit() {
  const ok = await auth.login({ ...form })
  if (!ok) return
  // Only accept in-app paths as a redirect target, never an absolute URL.
  const target = route.query.redirect
  const safe = typeof target === 'string' && target.startsWith('/') ? target : { name: 'home' }
  router.replace(safe)
}
</script>

<template>
  <form class="card auth-form stack" @submit.prevent="submit">
    <div>
      <h1>Вход</h1>
      <p class="text-muted">Используйте почту, указанную при регистрации.</p>
    </div>

    <BaseInput
      v-model="form.email"
      label="Почта"
      type="email"
      autocomplete="email"
      :error="auth.fieldErrors.email"
    />
    <BaseInput
      v-model="form.password"
      label="Пароль"
      type="password"
      autocomplete="current-password"
      :error="auth.fieldErrors.password"
    />

    <p v-if="auth.error" class="error">{{ auth.error }}</p>

    <BaseButton type="submit" :loading="auth.loading">Войти</BaseButton>

    <p class="text-muted">
      Нет аккаунта?
      <RouterLink :to="{ name: 'register' }" class="link">Зарегистрироваться</RouterLink>
    </p>
  </form>
</template>

<style scoped>
.auth-form {
  width: min(380px, 100%);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}

.link {
  color: var(--c-moss);
  font-weight: 600;
}
</style>
