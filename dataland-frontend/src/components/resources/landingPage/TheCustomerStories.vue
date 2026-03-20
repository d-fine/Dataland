<template>
  <section class="customer-stories" role="region" aria-labelledby="customer-stories-heading">
    <div class="customer-stories__wrapper">
      <h2 id="customer-stories-heading" class="customer-stories__heading">Customer stories</h2>
      <div class="customer-stories__grid">
        <article v-for="story in CUSTOMER_STORY_SUMMARIES" :key="story.tag" class="story-card">
          <img :src="story.logo" :alt="`${story.tag} logo`" class="story-card__logo" />
          <span class="story-card__tag">{{ story.tag }}</span>
          <p class="story-card__text">{{ story.text }}</p>
          <router-link :to="story.link" class="story-card__link">Read more</router-link>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { CUSTOMER_STORY_SUMMARIES } from '@/components/resources/landingPage/landingContent';
</script>

<style scoped lang="scss">
.customer-stories {
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

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }
}

.story-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;

  &__logo {
    height: 60px;
    object-fit: contain;
    align-self: flex-start;
  }

  &__tag {
    font-size: 0.875rem;
    color: var(--p-primary-color);
    background: var(--p-primary-50, #fff3e6);
    padding: 4px 12px;
    border-radius: 999px;
    display: inline-block;
    width: fit-content;
  }

  &__text {
    font-size: 1rem;
    line-height: 1.5;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__link {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--p-primary-color);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

@media only screen and (max-width: $bp-lg) {
  .customer-stories__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (max-width: $bp-md) {
  .customer-stories {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }

  .customer-stories__grid {
    grid-template-columns: 1fr;
  }
}
</style>
