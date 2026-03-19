<template>
  <section class="news-insights" role="region" aria-labelledby="news-insights-heading">
    <div class="news-insights__wrapper">
      <h2 id="news-insights-heading" class="news-insights__heading">News and insights</h2>
      <AccessibleCarousel
        :auto-scroll="true"
        :interval="7000"
        :visible-items="{ sm: 1, md: 2, lg: 3 }"
        :total-slots="NEWS_ITEMS.length"
        aria-label="News and insights carousel"
      >
        <template v-for="(item, index) in NEWS_ITEMS" :key="item.title" #[`slide-${index}`]>
          <NewsCard :image="item.image" :title="item.title" :date="item.date" :link="item.link" />
        </template>
      </AccessibleCarousel>
      <p class="news-insights__bottom-text">Want to stay up to date?</p>
      <div class="news-insights__actions">
        <a :href="URL_LINKEDIN_DATALAND" target="_blank" rel="noopener noreferrer">
          <Button label="Follow us on LinkedIn" rounded severity="secondary" />
        </a>
        <router-link to="/newsletter">
          <Button label="Subscribe to our Newsletter" rounded severity="secondary" />
        </router-link>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import AccessibleCarousel from '@/components/generics/AccessibleCarousel.vue';
import NewsCard from '@/components/generics/NewsCard.vue';
import Button from 'primevue/button';
import { NEWS_ITEMS } from '@/components/resources/landingPage/landingContent';
import { URL_LINKEDIN_DATALAND } from '@/components/resources/productPage/productContent';
</script>

<style scoped lang="scss">
.news-insights {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 32px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__bottom-text {
    font-size: 1rem;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
    text-align: center;
  }

  &__actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media only screen and (max-width: $bp-md) {
  .news-insights {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }
}
</style>
