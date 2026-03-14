<template>
  <section class="about-advisory" role="region" aria-labelledby="advisory-heading">
    <div class="about-advisory__wrapper">
      <h2 id="advisory-heading" class="about-advisory__heading">Advisory Board</h2>
      <div class="about-advisory__grid">
        <div v-for="person in ADVISORY_BOARD" :key="person.name" class="about-advisory__grid-item">
          <PersonCard :person="person" variant="advisory" />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ADVISORY_BOARD } from '@/components/resources/aboutPage/aboutContent';
import PersonCard from '@/components/resources/aboutPage/PersonCard.vue';
</script>

<style scoped lang="scss">
.about-advisory {
  padding: 4rem 2rem;
  background: var(--grey-tones-100, #f6f6f6);
  border-bottom: 1px solid var(--p-surface-200, #dadada);

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
    text-align: center;
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  &__grid-item {
    display: flex;
    justify-content: center;
  }
}

@media only screen and (max-width: $bp-md) {
  .about-advisory {
    padding: 2.5rem 1rem;
  }
}

@media only screen and (min-width: $bp-md) {
  .about-advisory__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (min-width: $bp-lg) {
  .about-advisory__grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
