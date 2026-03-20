<template>
  <section id="features" class="product-features" role="region" aria-labelledby="product-features-heading">
    <div class="product-features__wrapper">
      <h2 id="product-features-heading" class="product-features__heading">Platform features</h2>
      <div class="product-features__grid">
        <div v-for="(card, index) in FEATURE_CARDS" :key="index" class="feature-card">
          <h3 class="feature-card__title">{{ card.title }}</h3>
          <p class="feature-card__subtitle">{{ card.subtitle }}</p>
          <p class="feature-card__text">{{ card.text }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { FEATURE_CARDS } from '@/components/resources/productPage/productContent';
</script>

<style scoped lang="scss">
.product-features {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 32px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    width: 100%;
  }
}

.feature-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  padding: 24px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 40px;
    height: 40px;
    background: var(--p-primary-color);
    opacity: 0.15;
    clip-path: polygon(100% 0, 0 0, 100% 100%);
  }

  &__title {
    font-size: 1.25rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__subtitle {
    font-size: 1.25rem;
    color: var(--p-text-muted-color, #737373);
    margin: 4px 0 0;
  }

  &__text {
    font-size: 0.875rem;
    line-height: 1.5;
    margin: 12px 0 0;
    color: var(--p-text-muted-color, #737373);
  }
}

@media only screen and (max-width: $bp-lg) {
  .product-features__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (max-width: $bp-md) {
  .product-features {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }

  .product-features__grid {
    grid-template-columns: 1fr;
  }
}
</style>
