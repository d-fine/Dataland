<template>
  <section id="updates" class="about-updates" role="region" aria-labelledby="updates-heading">
    <div class="about-updates__wrapper">
      <h2 id="updates-heading" class="about-updates__heading">News and updates</h2>
      <div class="about-updates__grid">
        <NewsCard
          v-for="item in NEWS_ITEMS"
          :key="item.title"
          :image="item.image"
          :title="item.title"
          :date="item.date"
          :link="item.link"
        />
      </div>
      <p class="about-updates__bottom-text">Follow our latest developments, events and product updates</p>
      <div class="about-updates__actions">
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
import NewsCard from '@/components/generics/NewsCard.vue';
import Button from 'primevue/button';
import { NEWS_ITEMS } from '@/components/resources/landingPage/landingContent';
import { URL_LINKEDIN_DATALAND } from '@/components/resources/productPage/productContent';
</script>

<style scoped lang="scss">
.about-updates {
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

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    width: 100%;
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

@media only screen and (max-width: $bp-lg) {
  .about-updates__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (max-width: $bp-md) {
  .about-updates {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }

  .about-updates__grid {
    grid-template-columns: 1fr;
  }
}
</style>
