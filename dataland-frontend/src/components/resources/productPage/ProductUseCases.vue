<template>
  <section id="use-cases" class="product-usecases" role="region" aria-labelledby="product-usecases-heading">
    <div class="product-usecases__wrapper">
      <h2 id="product-usecases-heading" class="product-usecases__heading">Use Cases</h2>
      <div class="product-usecases__list">
        <article
          v-for="(useCase, index) in USE_CASES"
          :key="index"
          class="use-case-block"
          :class="{ 'use-case-block--reversed': index % 2 !== 0 }"
        >
          <div class="use-case-block__content">
            <h3 class="use-case-block__title">{{ useCase.title }}</h3>
            <p class="use-case-block__text">{{ useCase.description }}</p>
          </div>
          <div class="use-case-block__illustration">
            <!-- [PLACEHOLDER] Use case illustration to be provided by design team -->
            <div class="use-case-block__placeholder" :aria-label="`Illustration for ${useCase.title}`"></div>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { USE_CASES } from '@/components/resources/productPage/productContent';
</script>

<style scoped lang="scss">
.product-usecases {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 48px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 48px;
    width: 100%;
  }
}

.use-case-block {
  display: grid;
  grid-template-columns: 55% 45%;
  gap: 32px;
  align-items: center;

  &--reversed {
    direction: rtl;

    .use-case-block__content,
    .use-case-block__illustration {
      direction: ltr;
    }
  }

  &__content {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  &__title {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__text {
    font-size: 1rem;
    line-height: 1.6;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
  }

  &__illustration {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  &__placeholder {
    width: 100%;
    height: 200px;
    background: var(--p-surface-100, #f0f0f0);
    border-radius: 12px;
    border: 1px dashed var(--p-surface-300, #c0c0c0);
  }
}

@media only screen and (max-width: $bp-md) {
  .product-usecases {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }

  .use-case-block {
    grid-template-columns: 1fr;

    &--reversed {
      direction: ltr;
    }

    &__title {
      font-size: 1.25rem;
    }

    &__illustration {
      order: -1;
    }
  }
}
</style>
