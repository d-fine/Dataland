<template>
  <section class="about-sponsors" role="region" aria-labelledby="sponsors-heading">
    <div class="about-sponsors__wrapper">
      <h2 id="sponsors-heading" class="about-sponsors__heading">Sponsors</h2>
      <div class="about-sponsors__logos">
        <LogoChip v-for="logo in SPONSORS" :key="logo.name" :logo="logo" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { SPONSORS } from '@/components/resources/aboutPage/aboutContent';
import LogoChip from '@/components/resources/aboutPage/LogoChip.vue';
</script>

<style scoped lang="scss">
.about-sponsors {
  padding: 4rem 2rem;
  background: var(--p-surface-0, #ffffff);
  border-bottom: 1px solid var(--p-surface-200, #dadada);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 2rem;
    align-items: center;
  }

  &__heading {
    font-size: 1.75rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    align-self: flex-start;
  }

  &__logos {
    display: flex;
    flex-wrap: wrap;
    gap: 2rem 3rem;
    justify-content: center;
    align-items: center;
    width: 100%;
  }
}

@media only screen and (max-width: 768px) {
  .about-sponsors {
    padding: 2.5rem 1rem;
  }
}
</style>