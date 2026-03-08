<template>
  <section class="about-hero" role="region" aria-labelledby="hero-heading">
    <div class="about-hero__content">
      <h1 id="hero-heading" class="about-hero__headline">{{ HERO_COPY.headline }}</h1>
      <p class="about-hero__subheadline">{{ HERO_COPY.subheadline }}</p>
      <Button
        :label="HERO_COPY.ctaLabel"
        class="about-hero__cta"
        aria-label="Get in touch with Dataland"
        rounded
        @click="openModal"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { HERO_COPY } from '@/components/resources/aboutPage/aboutContent';
import { useContactModal } from '@/composables/useContactModal';

const { openModal } = useContactModal();
</script>

<style scoped lang="scss">
.about-hero {
  padding: 5rem 2rem 4rem;
  background: var(--p-surface-0, #ffffff);
  border-bottom: 1px solid var(--p-surface-200, #dadada);

  &__content {
    max-width: 800px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 1.5rem;
  }

  &__headline {
    font-size: 3rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    line-height: 1.15;
  }

  &__subheadline {
    font-size: 1.125rem;
    font-weight: 400;
    margin: 0;
    color: var(--p-text-muted-color, #585858);
    line-height: 1.6;
  }

  &__cta {
    min-height: 48px;
  }
}

@media only screen and (max-width: $bp-md) {
  .about-hero {
    padding: 3rem 1rem 2.5rem;

    &__headline {
      font-size: 2rem;
    }

    &__cta {
      width: 100%;
    }
  }
}
</style>