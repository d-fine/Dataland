<template>
  <div class="logo-chip">
    <img
      v-if="!imageError"
      :src="logo.imagePath"
      :alt="logo.name"
      class="logo-chip__img"
      @error="imageError = true"
    />
    <div v-else class="logo-chip__fallback">{{ logo.name }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { Logo } from '@/components/resources/aboutPage/aboutContent';

defineProps<{
  logo: Logo;
}>();

const imageError = ref(false);
</script>

<style scoped lang="scss">
.logo-chip {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;

  &__img {
    height: 48px;
    width: auto;
    max-width: 160px;
    object-fit: contain;
    display: block;
  }

  &__fallback {
    height: 48px;
    padding: 0 1rem;
    border: 1px solid var(--p-surface-200, #dadada);
    border-radius: 4px;
    display: flex;
    align-items: center;
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #585858);
    background: var(--p-surface-50, #f9f9f9);
    white-space: nowrap;
  }
}
</style>