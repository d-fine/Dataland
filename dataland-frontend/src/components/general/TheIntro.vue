<template>
  <section v-if="introSection" class="intro" role="region" aria-label="Introduction">
    <img
      v-for="(img, index) in introSection.image"
      :key="index"
      :src="img"
      :alt="introSection.text.join(' ')"
      class="intro__img"
    />

    <h1 class="intro__text">
      <template v-for="(part, index) in introSection.text" :key="index">
        <span v-if="index === 0 || index === 2">{{ part }}</span>
        <template v-else>{{ part }}</template>
      </template>
    </h1>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const introSection = computed(() => {
  return sections?.find((section) => section.title === "Intro") ?? null;
});
</script>

<style scoped lang="scss">
.intro {
  text-align: center;
  margin: 140px auto;
  max-width: 1007px;
  &__img {
    width: 85px;
    height: auto;
    margin-bottom: 42px;
  }
  &__text {
    color: var(--grey-tones-400);
    text-align: center;
    font-size: 100px;
    font-style: normal;
    font-weight: 700;
    line-height: 106px;
    letter-spacing: 0.25px;
    margin: 0;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    span:first-of-type {
      color: var(--basic-dark);
    }
    span:last-of-type {
      display: block;
      font-size: 48px;
      line-height: 56px; /* 116.667% */
      margin-top: 80px;
      color: var(--basic-dark);
    }
  }
}
@media only screen and (max-width: $large) {
  .intro {
    &__img {
      margin-bottom: 31px;
    }
    &__text {
      font-size: 64px;
      line-height: 78px;
      max-width: 750px;
      margin: 0 auto;
      span:last-of-type {
        font-size: 40px;
        font-weight: 600;
        line-height: 48px; /* 120% */
        letter-spacing: 0.25px;
        margin-top: 31px;
      }
    }
  }
}
</style>
