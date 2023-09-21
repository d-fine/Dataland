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
        <span v-if="index === 0">{{ part }}</span>
        <template v-else>{{ part }}</template>
      </template>
    </h1>
    <TheSearch v-if="introCard" :icon="introCard.icon ?? ''" :placeholderText="introCard.text" />
    <button class="intro__button">START YOUR DATALAND JOURNEY</button>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";
import TheSearch from "./TheSearch.vue";

const { sections } = defineProps<{ sections?: Section[] }>();

const introSection = computed(() => {
  return sections?.find((section) => section.title === "Intro") || null;
});

const introCard = computed(() => {
  return introSection.value?.cards?.find((card) => card.icon && card.text) || null;
});
</script>

<style scoped lang="scss">
.intro {
  text-align: center;
  margin: 120px auto;
  max-width: 1007px;
  &__img {
    width: 81px;
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
    span {
      color: var(--basic-dark);
    }
  }
  &__button {
    padding: 14px 32px;
    border-radius: 32px;
    background-color: var(--primary-orange);
    color: var(--default-neutral-white);
    font-size: 16px;
    font-style: normal;
    font-weight: 600;
    line-height: 20px;
    letter-spacing: 0.75px;
    text-transform: uppercase;
    border: 2px solid var(--primary-orange);
    cursor: pointer;
    margin-top: 64px; //spacing
    &:hover {
      background-color: var(--default-neutral-white);
      color: var(--basic-dark);
    }
  }
}
</style>
