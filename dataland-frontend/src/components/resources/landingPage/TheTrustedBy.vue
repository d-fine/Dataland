<template>
  <section class="trusted-by" role="region" aria-labelledby="trusted-by-heading">
    <div class="trusted-by__wrapper">
      <h2 id="trusted-by-heading" class="trusted-by__heading">Trusted by institutions across sectors and sizes</h2>
      <AccessibleCarousel
        :auto-scroll="true"
        :interval="7000"
        :visible-items="{ sm: 1, md: 2, lg: 4 }"
        :total-slots="TRUSTED_BY_LOGOS.length"
        aria-label="Trusted by logos carousel"
      >
        <template v-for="(logo, index) in TRUSTED_BY_LOGOS" :key="logo.name" #[`slide-${index}`]>
          <router-link to="/product#customer-stories" class="trusted-by__logo-link">
            <img :src="logo.imagePath" :alt="logo.name" class="trusted-by__logo-img" />
          </router-link>
        </template>
      </AccessibleCarousel>
    </div>
  </section>
</template>

<script setup lang="ts">
import AccessibleCarousel from '@/components/generics/AccessibleCarousel.vue';
import { TRUSTED_BY_LOGOS } from '@/components/resources/landingPage/landingContent';
</script>

<style scoped lang="scss">
.trusted-by {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 32px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__logo-link {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80px;
    text-decoration: none;
  }

  &__logo-img {
    max-height: 60px;
    max-width: 160px;
    object-fit: contain;
  }
}

@media only screen and (max-width: $bp-md) {
  .trusted-by {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }
}
</style>
