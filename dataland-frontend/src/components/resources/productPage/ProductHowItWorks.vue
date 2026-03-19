<template>
  <section id="how-it-works" class="product-how" role="region" aria-labelledby="product-how-heading">
    <div class="product-how__wrapper">
      <h2 id="product-how-heading" class="product-how__heading">How it works</h2>
      <div class="product-how__blocks">
        <ProblemSolutionBlock
          v-for="(block, index) in HOW_IT_WORKS_BLOCKS"
          :key="index"
          :problem-title="block.leftTitle"
          :problem-text="block.leftText"
          :solution-title="block.rightTitle"
          :solution-text="block.rightText"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import ProblemSolutionBlock from '@/components/generics/ProblemSolutionBlock.vue';
import { HOW_IT_WORKS_BLOCKS } from '@/components/resources/productPage/productContent';
</script>

<style scoped lang="scss">
.product-how {
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

  &__blocks {
    display: flex;
    flex-direction: column;
    gap: 24px;
    width: 100%;
  }
}

@media only screen and (max-width: $bp-md) {
  .product-how {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }
}
</style>
