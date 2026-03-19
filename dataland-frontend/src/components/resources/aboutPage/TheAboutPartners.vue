<template>
  <section id="partners" class="about-partners" role="region" aria-labelledby="partners-heading">
    <div class="about-partners__wrapper">
      <h2 id="partners-heading" class="about-partners__heading">Integration partners</h2>
      <div class="about-partners__grid">
        <a
          v-for="partner in PARTNERS"
          :key="partner.name"
          :href="partner.url"
          target="_blank"
          rel="noopener noreferrer"
          class="about-partners__logo-link"
          :aria-label="`Visit ${partner.name} website`"
        >
          <img :src="partner.imagePath" :alt="partner.name" class="about-partners__logo-img" />
        </a>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { PARTNERS } from '@/components/resources/aboutPage/aboutContent';
</script>

<style scoped lang="scss">
.about-partners {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 40px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 48px;
    max-width: 600px;
    width: 100%;
  }

  &__logo-link {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__logo-img {
    height: 120px;
    object-fit: contain;
    max-width: 100%;
  }
}

@media only screen and (max-width: $bp-md) {
  .about-partners {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }

    &__grid {
      grid-template-columns: 1fr;
      gap: 32px;
    }
  }
}
</style>
