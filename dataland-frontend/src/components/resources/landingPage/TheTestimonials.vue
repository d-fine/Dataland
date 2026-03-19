<template>
  <section class="testimonials" role="region" aria-labelledby="testimonials-heading">
    <div class="testimonials__wrapper">
      <h2 id="testimonials-heading" class="testimonials__heading">What our members share about their experience</h2>
      <AccessibleCarousel
        :auto-scroll="true"
        :interval="7000"
        :visible-items="{ sm: 1, md: 2, lg: 4 }"
        :total-slots="TESTIMONIALS.length"
        aria-label="Member testimonials carousel"
      >
        <template v-for="(testimonial, index) in TESTIMONIALS" :key="testimonial.author" #[`slide-${index}`]>
          <article class="testimonial-card">
            <blockquote class="testimonial-card__quote">"{{ testimonial.quote }}"</blockquote>
            <p class="testimonial-card__author">{{ testimonial.author }}</p>
            <p class="testimonial-card__affiliation">{{ testimonial.affiliation }}</p>
          </article>
        </template>
      </AccessibleCarousel>
      <div class="testimonials__cta">
        <router-link to="/testimonials">
          <Button label="Watch member testimonials" rounded severity="secondary" />
        </router-link>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import AccessibleCarousel from '@/components/generics/AccessibleCarousel.vue';
import Button from 'primevue/button';
import { TESTIMONIALS } from '@/components/resources/landingPage/landingContent';
</script>

<style scoped lang="scss">
.testimonials {
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

  &__cta {
    display: flex;
    justify-content: center;
  }
}

.testimonial-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 12px;
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;

  &__quote {
    font-size: 1.25rem;
    font-style: italic;
    line-height: 1.5;
    margin: 0 0 16px;
    color: var(--p-text-color, #1b1b1b);
    flex: 1;
  }

  &__author {
    font-size: 0.875rem;
    font-weight: 700;
    text-align: right;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__affiliation {
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #737373);
    text-align: right;
    margin: 4px 0 0;
  }
}
</style>
