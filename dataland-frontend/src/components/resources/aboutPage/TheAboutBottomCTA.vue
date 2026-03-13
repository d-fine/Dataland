<template>
  <section class="about-bottom-cta" role="region" aria-labelledby="bottom-cta-heading">
    <div class="about-bottom-cta__content">
      <h2 id="bottom-cta-heading" class="about-bottom-cta__headline">{{ BOTTOM_CTA_COPY.headline }}</h2>
      <p class="about-bottom-cta__subheadline">{{ BOTTOM_CTA_COPY.subheadline }}</p>
      <div class="about-bottom-cta__actions">
        <Button
          :label="BOTTOM_CTA_COPY.primaryCtaLabel"
          class="about-bottom-cta__cta"
          aria-label="Talk to our team"
          rounded
          @click="() => openEmailClient(aboutEmailCard)"
        />
        <Button
          :label="BOTTOM_CTA_COPY.secondaryCtaLabel"
          class="about-bottom-cta__cta"
          aria-label="Learn more about our data"
          severity="secondary"
          rounded
          @click="navigateToFrameworks"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { useRouter } from 'vue-router';
import { BOTTOM_CTA_COPY } from '@/components/resources/aboutPage/aboutContent';
import { openEmailClient, aboutEmailCard } from '@/utils/Email';

const router = useRouter();

const navigateToFrameworks = (): void => {
  void router.push({ path: '/', hash: '#frameworks' });
};
</script>

<style scoped lang="scss">
.about-bottom-cta {
  padding: 5rem 2rem;
  background: var(--p-surface-0, #ffffff);
  text-align: center;

  &__content {
    max-width: 600px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.5rem;
  }

  &__headline {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__subheadline {
    font-size: 1.125rem;
    font-weight: 400;
    margin: 0;
    color: var(--p-text-muted-color, #585858);
    line-height: 1.6;
  }

  &__actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
    flex-wrap: wrap;
  }

  &__cta {
    min-height: 48px;
  }
}

@media only screen and (max-width: $bp-md) {
  .about-bottom-cta {
    padding: 3rem 1rem;

    &__actions {
      flex-direction: column;
      width: 100%;
    }

    &__cta {
      width: 100%;
    }
  }
}
</style>