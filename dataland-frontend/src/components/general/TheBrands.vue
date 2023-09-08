<template>
  <section v-if="brandsSection" class="brands" role="region" aria-label="Brands Statement">
    <div class="brands__wrap">
      <h2 id="brands-heading" aria-labelledby="brands-heading" class="brands__text">{{ brandsSection.text[0] }}</h2>
      <div class="brands__list" role="list">
        <div class="brands__item" v-for="(imgSrc, index) in brandsSection.image" :key="index" role="listitem">
          <img :src="imgSrc" :alt="`Brand ${index + 1}`" class="brands__item-image" />
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
  background-color: #f6f6f6;
  gap: 40px;

  &__wrap {
    max-width: 1200px;
    width: 100%;
  }

  &__text {
    font-size: 24px;
    font-style: normal;
    font-weight: 600;
    line-height: 32px; /* 133.333% */
    letter-spacing: 0.25px;
    text-align: left;
    margin: 0 0 40px;
  }

  &__list {
    display: flex;
    flex-direction: row;
    gap: 32px;

    .brands__item {
      width: 200px;
      height: 124px;
      padding: 16px;
      background-color: #fff;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      &--image {
        max-width: 200px;
        height: auto;
      }
    }
  }
}
</style>
