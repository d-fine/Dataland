<template>
  <article class="news-card">
    <img :src="image" :alt="title" class="news-card__image" />
    <div class="news-card__body">
      <h3 class="news-card__title">{{ title }}</h3>
      <p class="news-card__date">{{ date }}</p>
      <a
        :href="link"
        target="_blank"
        rel="noopener noreferrer"
        class="news-card__link"
        :aria-label="`Read more about ${title}`"
      >
        Read more
      </a>
    </div>
  </article>
</template>

<script setup lang="ts">
defineProps<{
  image: string;
  title: string;
  date: string;
  link: string;
}>();
</script>

<style scoped lang="scss">
.news-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  overflow: hidden;

  &__image {
    width: 100%;
    height: 220px;
    object-fit: cover;
    display: block;
  }

  &__body {
    padding: 16px;
  }

  &__title {
    font-size: 1.25rem;
    font-weight: 700;
    line-height: 1.4;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__date {
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #737373);
    margin: 8px 0 0;
  }

  &__link {
    font-size: 0.875rem;
    color: var(--p-primary-color);
    font-weight: 600;
    margin-top: 8px;
    display: inline-block;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
