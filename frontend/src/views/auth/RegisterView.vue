<script setup>
import { reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const auth = useAuthStore()
const router = useRouter()

// Matches the expected POST /api/auth/register body (stage 3).
const form = reactive({ fullName: '', email: '', password: '' })

// Stage 3: validation, password rules and role selection come from the backend contract.
async function submit() {
  const ok = await auth.register({ ...form })
  if (ok) router.push({ name: 'login' })
}
</script>

<template>
  <form class="card auth-form stack" @submit.prevent="submit">
    <div>
      <h1>Регистрация</h1>
      <p class="text-muted">Один аккаунт для поездок, отходов и вакансий.</p>
    </div>

    <BaseInput
      v-model="form.fullName"
      label="Имя и фамилия"
      autocomplete="name"
      :error="auth.fieldErrors.fullName"
    />
    <BaseInput
      v-model="form.email"
      label="Почта"
      type="email"
      autocomplete="email"
      :error="auth.fieldErrors.email"
    />
    <BaseInput v-model="form.password" label="Пароль" type="password" autocomplete="new-password" />

    <p v-if="auth.error" class="error">{{ auth.error }}</p>

    <BaseButton type="submit" :loading="auth.loading">Создать аккаунт</BaseButton>

    <p class="text-muted">
      Уже есть аккаунт?
      <RouterLink :to="{ name: 'login' }" class="link">Войти</RouterLink>
    </p>
  </form>
</template>

<style scoped>
.auth-form {
  width: min(420px, 100%);
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
