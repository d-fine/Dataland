<template>
  <section class="about-principles" role="region" aria-labelledby="principles-heading">
    <div class="about-principles__wrapper">
      <h2 id="principles-heading" class="about-principles__heading">Our Principles</h2>
      <div class="about-principles__grid">
        <div v-for="principle in PRINCIPLES" :key="principle.title" class="about-principles__card">
          <i :class="principle.icon" aria-hidden="true" class="about-principles__card-icon" />
          <h3 class="about-principles__card-title">{{ principle.title }}</h3>
          <p class="about-principles__card-description">{{ principle.description }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { PRINCIPLES } from '@/components/resources/aboutPage/aboutContent';
</script>

<style scoped lang="scss">
.about-principles {
  padding: 4rem 2rem;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }

  &__heading {
    font-size: 1.75rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
  }

  &__card {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
    padding: 1.5rem;
    background: var(--p-surface-0, #ffffff);
    border: 1px solid var(--p-surface-200, #dadada);
    border-left: 3px solid #ff6813;
    border-radius: 0.75rem;
    text-align: left;
  }

  &__card-icon {
    font-size: 1.5rem;
    color: #ff6813;
  }

  &__card-title {
    font-size: 1.125rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__card-description {
    font-size: 1rem;
    font-weight: 400;
    color: var(--p-text-muted-color, #6b7280);
    line-height: 1.6;
    margin: 0;
  }
}

@media only screen and (max-width: $bp-lg) {
  .about-principles {
    &__grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .about-principles {
    padding: 2.5rem 1rem;

    &__grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
