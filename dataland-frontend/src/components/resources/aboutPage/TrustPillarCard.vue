<template>
  <div class="trust-pillar-card">
    <i :class="pillar.icon" aria-hidden="true" class="trust-pillar-card__icon" />
    <h3 class="trust-pillar-card__title">{{ pillar.title }}</h3>
    <p class="trust-pillar-card__description">{{ pillar.description }}</p>
  </div>
</template>

<script setup lang="ts">
import type { TrustPillar } from '@/components/resources/aboutPage/aboutContent';

defineProps<{
  pillar: TrustPillar;
}>();
</script>

<style scoped lang="scss">
.trust-pillar-card {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.5rem;
  border: 1px solid var(--p-surface-200, #dadada);
  border-radius: 0.75rem;
  background: var(--p-surface-0, #ffffff);

  &__icon {
    font-size: 2rem;
    color: var(--p-primary-color, #ff6813);
  }

  &__title {
    font-size: 1.125rem;
    font-weight: 600;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__description {
    font-size: 1rem;
    font-weight: 400;
    margin: 0;
    color: var(--p-text-muted-color, #585858);
    line-height: 1.6;
  }
}
</style>