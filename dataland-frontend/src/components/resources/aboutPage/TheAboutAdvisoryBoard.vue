<template>
  <section class="about-advisory" role="region" aria-labelledby="advisory-heading">
    <div class="about-advisory__wrapper">
      <h2 id="advisory-heading" class="about-advisory__heading">Advisory Board</h2>
      <div class="grid">
        <div v-for="person in ADVISORY_BOARD" :key="person.name" class="col-12 md:col-6 xl:col-4">
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
  background: #ffefe7;
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
  }
}

@media only screen and (max-width: 768px) {
  .about-advisory {
    padding: 2.5rem 1rem;
  }
}
</style>