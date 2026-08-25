<script setup>
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AdminAlertToast from '@/components/layout/AdminAlertToast.vue'
import { useAuthStore } from '@/stores/auth'
import { ROLES } from '@/utils/constants'

const auth = useAuthStore()
const isAdmin = computed(() => auth.hasRole(ROLES.ADMIN))
</script>

<template>
  <div class="layout">
    <AppHeader />

    <!-- Компонент создаётся только для ADMIN, иначе backend запретит подписку. -->
    <AdminAlertToast v-if="isAdmin" />

    <main class="layout__main container">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.layout__main {
  flex: 1;
  padding-block: var(--space-6) var(--space-8);
}
</style>
