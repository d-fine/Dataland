<template>
  <section v-if="struggleSection" class="struggle" role="region" aria-label="Struggle Statement">
    <div class="struggle__wrapper">
      <h2 id="struggle-heading" aria-labelledby="struggle-heading">
        <span>{{ struggleSection.text[0] }}</span>
        {{ struggleSection.text[1] }}
      </h2>
      <div class="struggle__grid" role="list">
        <div v-for="(card, index) in struggleSection.cards" :key="index" class="struggle__cell" role="listitem">
          <img :src="card.icon" :alt="card.title" class="struggle__cell__icon" />
          <h3 class="struggle__cell__title">{{ card.title }}</h3>
          <p class="struggle__cell__text">{{ card.text }}</p>
        </div>
      </div>
    </div>
  </section>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { Section } from '@/types/ContentTypes';

const { sections } = defineProps<{ sections?: Section[] }>();

const struggleSection = computed(() => {
  return sections?.find((section) => section.title === 'Struggle') ?? null;
});
</script>
<style scoped lang="scss">
@use '@/assets/scss/newVariables' as *;

.struggle {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120px 0;
  background-color: var(--grey-tones-100);
  gap: 68px;
  &__wrapper {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 32px;
    max-width: 900px;
    width: 100%;
    padding: 0 32px;
  }
  h2 {
    grid-column: 1 / -1;
    font-size: 100px;
    font-style: normal;
    font-weight: 700;
    line-height: 106px; /* 106% */
    letter-spacing: 0.25px;

    margin: 0 0 68px;
    text-align: left;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    span {
      color: var(--primary-orange);
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 48px 32px;
    grid-column: 1 / -1;
  }

  &__cell {
    display: flex;
    flex-direction: column;
    align-items: start;
    padding-right: 88px;

    &__icon {
      width: 48px;
      height: 48px;
      margin-bottom: 24px;
    }

    &__title {
      font-size: 32px;
      font-style: normal;
      font-weight: 600;
      line-height: 40px; /* 125% */
      letter-spacing: 0.25px;
      text-align: left;
      margin: 0 0 8px;
    }

    &__text {
      font-size: 20px;
      font-style: normal;
      font-weight: 400;
      line-height: 28px; /* 140% */
      letter-spacing: 0.25px;
      color: var(--grey-tones-700);
      text-align: left;
      margin: 0;
    }
  }
}

@media only screen and (max-width: $medium) {
  .struggle {
    &__wrapper {
      grid-template-columns: repeat(12, 1fr);
      padding: 0 22px;
      gap: 40px 22px;
    }
    h2 {
      grid-column: 2 / 10;
      font-size: 64px;
      line-height: 72px;
      letter-spacing: 0.25px;
      margin: 0;
    }
    &__grid {
      grid-column: 2 / 12;
      gap: 40px 22px;
    }
    &__cell {
      padding-right: 0;
      &__icon {
        width: 40px;
        height: 40px;
      }
    }
  }
}

@media only screen and (max-width: $small) {
  .struggle {
    padding: 80px 0;
    &__wrapper {
      gap: 56px 16px;
      padding: 0 16px;
    }
    h2 {
      font-size: 32px;
      line-height: 40px;
    }
    &__grid {
      grid-template-columns: 1fr;
    }
    &__cell {
      &__icon {
        margin-bottom: 16px;
      }
      &__title {
        font-size: 20px;
        line-height: 28px;
      }
    }
  }
}
</style>
