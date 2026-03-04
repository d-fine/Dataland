<template>
  <section class="about-partners" role="region" aria-labelledby="partners-heading">
    <div class="about-partners__wrapper">
      <h2 id="partners-heading" class="about-partners__heading">Partners</h2>
      <div class="about-partners__logos">
        <LogoChip v-for="logo in PARTNERS" :key="logo.name" :logo="logo" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { PARTNERS } from '@/components/resources/aboutPage/aboutContent';
import LogoChip from '@/components/resources/aboutPage/LogoChip.vue';
</script>

<style scoped lang="scss">
.about-partners {
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
  .about-partners {
    padding: 2.5rem 1rem;
  }
}
</style>