<template>
  <section v-if="brandsSection" class="brands" role="region" aria-label="Brands Statement">
    <div class="brands__wrap">
      <h2 id="brands-heading" aria-labelledby="brands-heading" class="brands__text">
        {{ brandsSection.text[0] }}
        <span>{{ brandsSection.text[1] }}</span>
      </h2>
      <div class="brands__list" role="list">
        <div class="brands__item" v-for="(imgSrc, index) in brandsSection.image" :key="index" role="listitem">
          <img :src="imgSrc" :alt="`Brand ${index + 1}`" :class="`brands__item-image brands__item-image--${index}`" />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const brandsSection = computed(() => {
  return sections?.find((section) => section.title === "Brands") || null;
});
</script>

<style scoped lang="scss">
.brands {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64px 0;
  gap: 40px;

  &__wrap {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__text {
    max-width: 1060px;
    padding: 0px 220px 0px 172px;
    font-size: 64px;
    font-style: normal;
    font-weight: 700;
    line-height: 78px; /* 121.875% */
    text-align: left;
    margin: 0 0 40px;
    span {
      color: var(--grey-tones-400);
    }
  }

  &__list {
    display: flex;
    flex-direction: row;
    gap: 32px;

    .brands__item {
      width: 200px;
      height: 124px;
      padding: 16px;
      background-color: var(--grey-tones-100);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      &-image {
        max-width: 200px;
        height: auto;
      }
      &-image--0 {
        fill: #373d41;
      }
      &:hover {
        background: #ffebe0;

        .brands__item-image {
          fill: #f56a01;
        }
      }
    }
  }
}
</style>
