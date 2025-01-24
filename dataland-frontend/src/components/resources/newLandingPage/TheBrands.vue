<template>
  <section v-if="brandsSection" class="brands" role="region" aria-label="Brands Statement">
    <div class="brands__wrap">
      <h2 id="brands-heading" aria-labelledby="brands-heading" class="brands__text">
        {{ brandsSection.text[0] }}
        <span>{{ brandsSection.text[1] }}</span>
      </h2>
      <div class="brands__list" role="list">
        <div class="brands__item" v-for="(imgSrc, index) in brandsSection.image" :key="index" role="listitem">
          <img :src="imgSrc" :alt="`Brand ${index + 1}`" class="brands__item-image" />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Section } from '@/types/ContentTypes';

const { sections } = defineProps<{ sections?: Section[] }>();

const brandsSection = computed(() => {
  return sections?.find((section) => section.title === 'Brands') ?? null;
});
</script>

<style scoped lang="scss">
@use '@/assets/scss/newVariables' as *;

.brands {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0 140px;
  gap: 40px;

  &__wrap {
    display: grid;
    grid-template-columns: repeat(16, 1fr);
    gap: 40px 32px;
    max-width: 1440px;
    width: 100%;
    padding: 0 32px;
  }

  &__text {
    font-size: 64px;
    margin: 0;
    font-style: normal;
    font-weight: 700;
    line-height: 78px; /* 121.875% */
    text-align: left;
    grid-column: 4 / -1;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    span {
      color: var(--grey-tones-400);
    }
  }

  &__list {
    display: flex;
    flex-direction: row;
    width: 100%;
    justify-content: center;
    max-width: 1130px;
    grid-column: 2 / -2;
    flex-wrap: wrap;
    gap: 40px 32px;

    .brands__item {
      height: 124px;
      display: flex;
      align-items: center;
      justify-content: center;
      &-image {
        max-width: 200px;
        height: auto;
      }
    }
  }
}

@media only screen and (max-width: $large) {
  .brands {
    &__wrap {
      grid-template-columns: repeat(12, 1fr);
      gap: 22px;
      padding: 0 22px;
    }
    &__text {
      font-size: 48px;
      min-width: 420px;
      font-weight: 600;
      line-height: 56px;
      letter-spacing: 0.25px;
      grid-column: 3/-1;
      max-width: 500px;
    }
  }
}
@media only screen and (max-width: $medium) {
  .brands {
    padding: 32px 0 80px;
    gap: 24px;
    &__wrap {
      grid-template-columns: repeat(12, 1fr);
      gap: 22px;
      padding: 0 22px;
    }
    &__text {
      font-size: 32px;
      line-height: 40px;
      max-width: 328px;
      min-width: unset;
    }
    &__list {
      grid-column: 1 / -1;
      width: 100%;
      gap: 16px;
      .brands__item {
        &-image {
          width: 100%;
          max-width: 190px;
        }
      }
    }
  }
}

@media only screen and (max-width: $small) {
  .brands {
    &__wrap {
      gap: 24px 16px;
      padding: 0 0 0 16px;
    }
    &__list {
      flex-wrap: wrap;
      justify-content: flex-start;
      .brands__item {
        flex: 0 0 calc(33.3333% - 16px);
        height: unset;
        &-image {
          width: 100%;
        }
      }
    }
  }
}
</style>
