<template>
  <section id="customer-stories" class="product-stories" role="region" aria-labelledby="product-stories-heading">
    <div class="product-stories__wrapper">
      <h2 id="product-stories-heading" class="product-stories__heading">Customer stories</h2>
      <p class="product-stories__subheading">Selected examples of how different institutions use Dataland</p>
      <div class="product-stories__list">
        <article
          v-for="story in CUSTOMER_STORIES_DETAILED"
          :key="story.anchor"
          :id="story.anchor"
          class="customer-story-detail"
        >
          <div class="customer-story-detail__logo-col">
            <img :src="story.logo" :alt="`${story.tag} logo`" class="customer-story-detail__logo" />
          </div>
          <div class="customer-story-detail__content">
            <span class="customer-story-detail__tag">{{ story.tag }}</span>
            <h3 class="customer-story-detail__title">{{ story.title }}</h3>
            <p class="customer-story-detail__text">{{ story.summary }}</p>

            <h4 class="customer-story-detail__section-title">Challenge</h4>
            <p class="customer-story-detail__text">{{ story.challenge }}</p>

            <h4 class="customer-story-detail__section-title">Solution</h4>
            <p class="customer-story-detail__text">{{ story.solution }}</p>

            <h4 class="customer-story-detail__section-title">Value</h4>
            <p class="customer-story-detail__text">{{ story.value }}</p>

            <div class="customer-story-detail__quote-box">
              <p class="customer-story-detail__quote-box-text">"{{ story.quoteText }}"</p>
              <p class="customer-story-detail__quote-box-author">{{ story.quoteAuthor }}</p>
              <p class="customer-story-detail__quote-box-role">{{ story.quoteRole }}</p>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { CUSTOMER_STORIES_DETAILED } from '@/components/resources/productPage/productContent';
</script>

<style scoped lang="scss">
.product-stories {
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

  &__subheading {
    font-size: 1.125rem;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
    text-align: center;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 48px;
  }
}

.customer-story-detail {
  background: var(--p-surface-50, #f7f7f5);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 16px;
  padding: 32px;
  display: grid;
  grid-template-columns: 24% 76%;
  gap: 24px;

  &__logo-col {
    display: flex;
    justify-content: center;
    align-items: flex-start;
  }

  &__logo {
    width: 100%;
    height: 120px;
    object-fit: contain;
  }

  &__content {
    display: flex;
    flex-direction: column;
  }

  &__tag {
    font-size: 0.875rem;
    background: var(--p-primary-50, #fff3e6);
    color: var(--p-primary-color);
    padding: 4px 12px;
    border-radius: 999px;
    display: inline-block;
    width: fit-content;
  }

  &__title {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 8px 0 16px;
    color: var(--p-text-color, #1b1b1b);
  }

  &__section-title {
    font-size: 1rem;
    font-weight: 700;
    margin: 16px 0 8px;
    color: var(--p-text-color, #1b1b1b);
  }

  &__text {
    font-size: 1rem;
    color: var(--p-text-muted-color, #737373);
    line-height: 1.6;
    margin: 0;
  }

  &__quote-box {
    background: var(--p-surface-0, #ffffff);
    border: 1px solid var(--p-surface-200, #e6e6e6);
    border-radius: 12px;
    padding: 20px;
    margin-top: 16px;

    &-text {
      font-style: italic;
      line-height: 1.5;
      margin: 0;
      color: var(--p-text-color, #1b1b1b);
    }

    &-author {
      font-size: 0.875rem;
      font-weight: 700;
      margin: 12px 0 0;
      color: var(--p-text-color, #1b1b1b);
    }

    &-role {
      font-size: 0.875rem;
      color: var(--p-text-muted-color, #737373);
      margin: 2px 0 0;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .product-stories {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }

  .customer-story-detail {
    grid-template-columns: 1fr;
    padding: 20px;
  }
}
</style>
