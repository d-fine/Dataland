<template>
  <section class="about-team" role="region" aria-labelledby="team-heading">
    <div class="about-team__wrapper">
      <h2 id="team-heading" class="about-team__heading">Leadership Team</h2>
      <div class="about-team__grid">
        <div v-for="person in LEADERSHIP_TEAM" :key="person.name" class="about-team__grid-item">
          <PersonCard :person="person" variant="leadership" />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { LEADERSHIP_TEAM } from '@/components/resources/aboutPage/aboutContent';
import PersonCard from '@/components/resources/aboutPage/PersonCard.vue';
</script>

<style scoped lang="scss">
.about-team {
  padding: 4rem 2rem;
  background: var(--p-surface-0, #ffffff);
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
  .about-team {
    padding: 2.5rem 1rem;
  }
}

@media only screen and (min-width: $bp-md) {
  .about-team__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (min-width: $bp-lg) {
  .about-team__grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>