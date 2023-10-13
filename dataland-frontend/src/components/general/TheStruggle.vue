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
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const struggleSection = computed(() => {
  return sections?.find((section) => section.title === "Struggle") ?? null;
});
</script>
<style scoped lang="scss">
.struggle {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120px 0;
  background-color: var(--grey-tones-100);
  gap: 68px;
  &__wrapper {
    display: grid;
    grid-template-columns: repeat(16, 1fr);
    gap: 32px;
    max-width: 1440px;
    width: 100%;
    padding: 0 32px;
  }
  h2 {
    grid-column: 4 / 14;
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
    grid-column: 4 / 14;
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
      color: #5b5b5b;
      text-align: left;
      margin: 0;
    }
  }
}

@media only screen and (max-width: $large) {
  .struggle {
    &__wrapper {
      grid-template-columns: repeat(12, 1fr);
      gap: 22px;
      padding: 0 22px;
    }
    h2 {
      grid-column: 2 / 10;
    }
    &__grid {
      grid-column: 2 / 12;
      gap: 80px 22px;
    }
    &__cell {
      padding-right: calc((100% - 4 * 22px) / 2);
      &__icon {
        width: 40px;
        height: 40px;
      }
    }
  }
}
@media only screen and (max-width: $medium) {
  .struggle {
    h2 {
      font-size: 48px;
      font-weight: 600;
      line-height: 56px; /* 116.667% */
      letter-spacing: 0.25px;
    }
  }
}
</style>
