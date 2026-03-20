<template>
  <section id="why-us" class="why-us" role="region" aria-labelledby="why-us-heading">
    <div class="why-us__wrapper">
      <h2 id="why-us-heading" class="why-us__heading">
        Common challenges in sustainability data procurement. How Dataland addresses them.
      </h2>
      <div class="why-us__blocks">
        <ProblemSolutionBlock
          v-for="(pair, index) in WHY_US_PAIRS"
          :key="index"
          :problem-title="pair.problemTitle"
          :problem-text="pair.problemText"
          :solution-title="pair.solutionTitle"
          :solution-text="pair.solutionText"
        />
      </div>
      <div class="why-us__actions">
        <router-link to="/product#features" class="why-us__cta-link">
          <Button label="Discover platform features" rounded severity="secondary" />
        </router-link>
        <router-link to="/product#use-cases" class="why-us__cta-link">
          <Button label="Explore use cases" rounded severity="secondary" />
        </router-link>
        <router-link to="/about#contact" class="why-us__cta-link">
          <Button label="Get in touch" rounded severity="secondary" />
        </router-link>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import ProblemSolutionBlock from '@/components/generics/ProblemSolutionBlock.vue';
import Button from 'primevue/button';
import { WHY_US_PAIRS } from '@/components/resources/landingPage/landingContent';
</script>

<style scoped lang="scss">
.why-us {
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
    max-width: 900px;
  }

  &__blocks {
    display: flex;
    flex-direction: column;
    gap: 24px;
    width: 100%;
  }

  &__actions {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    width: 100%;
    max-width: 900px;
  }

  &__cta-link {
    text-decoration: none;
    display: flex;
    justify-content: center;
  }
}

@media only screen and (max-width: $bp-lg) {
  .why-us {
    &__actions {
      grid-template-columns: 1fr;
      max-width: 400px;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .why-us {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }
}
</style>
