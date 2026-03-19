<template>
  <section class="customer-profiles" role="region" aria-labelledby="customer-profiles-heading">
    <div class="customer-profiles__wrapper">
      <h2 id="customer-profiles-heading" class="customer-profiles__heading">Who uses Dataland</h2>
      <div class="customer-profiles__mosaic">
        <div
          v-for="tile in SECTOR_TILES"
          :key="tile.title"
          class="sector-tile"
          :class="`sector-tile--${tile.size.toLowerCase()}`"
        >
          <!-- [PLACEHOLDER] sector icon assets to be provided by design team -->
          <img :src="tile.icon" :alt="tile.title" class="sector-tile__icon" />
          <span class="sector-tile__title">{{ tile.title }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { SECTOR_TILES } from '@/components/resources/landingPage/landingContent';
</script>

<style scoped lang="scss">
.customer-profiles {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 32px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__mosaic {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 16px;
  }
}

.sector-tile {
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;

  &__icon {
    width: 48px;
    height: 48px;
    object-fit: contain;
  }

  &__title {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--p-text-color, #1b1b1b);
  }

  &--xl {
    grid-column: span 4;
  }

  &--l {
    grid-column: span 4;
  }

  &--m {
    grid-column: span 4;
  }

  &--s {
    grid-column: span 3;
  }
}

@media only screen and (max-width: $bp-lg) {
  .customer-profiles__mosaic {
    grid-template-columns: repeat(3, 1fr);
  }

  .sector-tile {
    &--xl,
    &--l,
    &--m,
    &--s {
      grid-column: span 1;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .customer-profiles {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }

  .customer-profiles__mosaic {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
