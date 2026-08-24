<script setup>
import { onMounted, ref } from 'vue'
import { usersAdminApi } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'
import { useAsync } from '@/composables/useAsync'
import { ROLES, ROLE_LABELS } from '@/utils/constants'
import { formatNumber } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'

const auth = useAuthStore()
const users = useAsync(usersAdminApi.findAll, [])
const saving = ref(null)
const error = ref('')

/**
 * UserRequestDto requires email, fullName and role together, so a role change
 * resends the existing email and name unchanged.
 */
async function changeRole(user, role) {
  if (role === user.role) return
  saving.value = user.id
  error.value = ''
  try {
    await usersAdminApi.update(user.id, {
      email: user.email,
      fullName: user.fullName,
      role,
    })
    await users.run()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = null
  }
}

onMounted(users.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Администрирование"
      title="Пользователи"
      subtitle="Регистрация всегда создаёт студента — роли назначаются здесь."
    />

    <p v-if="error" class="error">{{ error }}</p>

    <StateBlock
      :loading="users.loading.value"
      :error="users.error.value ?? ''"
      :empty="(users.data.value ?? []).length === 0"
      empty-title="Пользователей нет"
      @retry="users.run"
    >
      <div class="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Пользователь</th>
              <th class="num">EcoCoins</th>
              <th class="num">ESG</th>
              <th>Роль</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users.data.value" :key="user.id">
              <td>
                <p class="name">{{ user.fullName }}</p>
                <p class="text-muted email">{{ user.email }}</p>
              </td>
              <td class="metric num">{{ formatNumber(user.ecoCoinsBalance) }}</td>
              <td class="metric num">{{ user.esgRating }}</td>
              <td>
                <select
                  :value="user.role"
                  :disabled="saving === user.id || user.id === auth.userId"
                  @change="changeRole(user, $event.target.value)"
                >
                  <option v-for="role in Object.values(ROLES)" :key="role" :value="role">
                    {{ ROLE_LABELS[role] }}
                  </option>
                </select>
                <p v-if="user.id === auth.userId" class="text-muted email">это вы</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="text-muted hint">
        Пользователь увидит новую роль после повторного входа — она хранится в JWT.
      </p>
    </StateBlock>
  </div>
</template>

<style scoped>
.table-wrap {
  padding: 0;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: var(--space-3) var(--space-4);
  text-align: left;
  border-bottom: 1px solid var(--c-line);
  vertical-align: middle;
}

th {
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--c-ink-muted);
  font-weight: 600;
}

.num {
  text-align: right;
}

.name {
  font-weight: 600;
}

.email {
  font-size: var(--text-xs);
}

select {
  padding: var(--space-2);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
}

select:disabled {
  opacity: 0.6;
}

.hint {
  font-size: var(--text-sm);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}
</style>
