<template>
  <section class="about-ecosystem" role="region" aria-labelledby="ecosystem-heading">
    <div class="about-ecosystem__wrapper">
      <h2 id="ecosystem-heading" class="about-ecosystem__heading">Our Ecosystem</h2>

      <div class="about-ecosystem__group">
        <h3 class="about-ecosystem__group-label">Sponsors</h3>
        <div class="about-ecosystem__logos">
          <LogoChip v-for="logo in SPONSORS" :key="logo.name" :logo="logo" />
        </div>
      </div>

      <div class="about-ecosystem__group">
        <h3 class="about-ecosystem__group-label">Partners</h3>
        <div class="about-ecosystem__logos">
          <LogoChip v-for="logo in PARTNERS" :key="logo.name" :logo="logo" />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { SPONSORS, PARTNERS } from '@/components/resources/aboutPage/aboutContent';
import LogoChip from '@/components/resources/aboutPage/LogoChip.vue';
</script>

<style scoped lang="scss">
.about-ecosystem {
  padding: 4rem 2rem;
  background: var(--p-surface-0, #ffffff);
  border-bottom: 1px solid var(--p-surface-200, #dadada);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 3rem;
  }

  &__heading {
    font-size: 1.75rem;
    font-weight: 700;
    margin: 0;
  }

  &__group {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  &__group-label {
    font-size: 1.125rem;
    font-weight: 600;
    margin: 0;
    color: var(--p-text-muted-color, #585858);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  &__logos {
    display: flex;
    flex-wrap: wrap;
    gap: 2rem 3rem;
    justify-content: flex-start;
    align-items: center;
  }
}

@media only screen and (max-width: $bp-md) {
  .about-ecosystem {
    padding: 2.5rem 1rem;

    &__logos {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1.5rem;
    }
  }
}
</style>
