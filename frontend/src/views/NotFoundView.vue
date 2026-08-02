<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="card stack">
    <p class="eyebrow">404</p>
    <h1>Такой страницы нет</h1>
    <p class="text-muted">Проверьте адрес или вернитесь на главную.</p>
    <p><RouterLink :to="{ name: 'home' }" class="link">На главную</RouterLink></p>
  </section>
</template>

<style scoped>
.link {
  color: var(--c-moss);
  font-weight: 600;
}
</style>
