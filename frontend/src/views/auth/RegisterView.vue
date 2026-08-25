<script setup>
import { computed, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { PASSWORD_PATTERN } from '@/utils/constants'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const auth = useAuthStore()
const router = useRouter()

// Matches RegisterRequestDto exactly.
const form = reactive({ fullName: '', email: '', password: '' })

/**
 * The backend rejects weak passwords with a 400. Checking the same rule here
 * means the user sees the requirement before submitting, not after.
 */
const passwordValid = computed(() => PASSWORD_PATTERN.test(form.password))
const passwordHint = computed(() => {
  if (!form.password) return ''
  return passwordValid.value ? '' : 'Минимум 8 символов, заглавная, строчная и цифра'
})

async function submit() {
  if (!passwordValid.value) return
  // Register returns tokens, so the user is signed in straight away.
  const ok = await auth.register({ ...form })
  if (ok) router.replace({ name: 'home' })
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
    <BaseInput
      v-model="form.password"
      label="Пароль"
      type="password"
      autocomplete="new-password"
      :error="auth.fieldErrors.password || passwordHint"
    />

    <p v-if="auth.error" class="error">{{ auth.error }}</p>

    <BaseButton type="submit" :loading="auth.loading" :disabled="!passwordValid">
      Создать аккаунт
    </BaseButton>

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
