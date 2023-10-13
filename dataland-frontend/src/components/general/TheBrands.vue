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
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const brandsSection = computed(() => {
  return sections?.find((section) => section.title === "Brands") ?? null;
});
</script>

<style scoped lang="scss">
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
    grid-column: 4 / 11;
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
    justify-content: space-evenly;
    max-width: 1440px;
    grid-column: 1 / -1;

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
    &__list {
      flex-wrap: wrap;
      width: calc((190px * 3) + (24px * 2));
      justify-content: flex-start;
      gap: 24px;
      grid-column: 3 /15;
      .brands__item {
        &-image {
          width: 190px;
        }
      }
    }
  }
}
// @media only screen and (max-width: $medium) {
//   .brands {
//     &__wrap {
//       grid-template-columns: repeat(12, 1fr);
//       gap: 22px;
//       padding: 0 22px;
//     }
//   }
// }
</style>
