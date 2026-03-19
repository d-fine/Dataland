<template>
  <main id="main-content" role="main">
    <section class="testimonials-page" role="region" aria-labelledby="testimonials-page-heading">
      <div class="testimonials-page__wrapper">
        <h1 id="testimonials-page-heading" class="testimonials-page__heading">Video testimonials</h1>
        <p class="testimonials-page__subheading">What our members and partners say about Dataland</p>
        <div class="testimonials-page__grid">
          <article v-for="testimonial in TESTIMONIALS" :key="testimonial.author" class="testimonial-page-card">
            <div class="testimonial-page-card__video">
              <!-- [PLACEHOLDER] Video embed deferred to backlog (requires YouTube API / cookie consent) -->
              <div class="testimonial-page-card__video-placeholder">
                <i class="pi pi-video" aria-hidden="true" />
              </div>
            </div>
            <blockquote class="testimonial-page-card__quote">"{{ testimonial.quote }}"</blockquote>
            <p class="testimonial-page-card__author">{{ testimonial.author }}</p>
            <p class="testimonial-page-card__affiliation">{{ testimonial.affiliation }}</p>
          </article>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { TESTIMONIALS } from '@/components/resources/landingPage/landingContent';
</script>

<style scoped lang="scss">
main {
  margin-top: 132px;
  @media only screen and (max-width: $bp-md) {
    margin-top: 80px;
  }
}

.testimonials-page {
  padding: 80px 64px;

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 32px;
  }

  &__heading {
    font-size: 2.5rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__subheading {
    font-size: 1.125rem;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
    text-align: center;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }
}

.testimonial-page-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__video {
    width: 100%;
    aspect-ratio: 16 / 9;
    border-radius: 8px;
    overflow: hidden;
  }

  &__video-placeholder {
    width: 100%;
    height: 100%;
    background: var(--p-surface-100, #f0f0f0);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--p-text-muted-color, #737373);
    font-size: 2rem;
  }

  &__quote {
    font-size: 1rem;
    font-style: italic;
    line-height: 1.5;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    flex: 1;
  }

  &__author {
    font-size: 0.875rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__affiliation {
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #737373);
    margin: 0;
  }
}

@media only screen and (max-width: $bp-lg) {
  .testimonials-page__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (max-width: $bp-md) {
  .testimonials-page {
    padding: 40px 16px;

    &__heading {
      font-size: 2rem;
    }
  }

  .testimonials-page__grid {
    grid-template-columns: 1fr;
  }
}
</style>
