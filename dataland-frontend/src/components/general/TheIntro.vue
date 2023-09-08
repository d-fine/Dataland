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
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const introSection = computed(() => {
  return sections?.find((section) => section.title === "Intro") || null;
});
</script>

<style scoped lang="scss">
.intro {
  align-items: center;
  margin: 140px auto 120px;
  max-width: 1007px;
  &__img {
    width: 81px;
    height: auto;
    margin-bottom: 42px;
  }
  &__text {
    color: #1b1b1b;
    text-align: center;
    font-size: 100px;
    font-style: normal;
    font-weight: 300;
    line-height: 106px;
    letter-spacing: 0.25px;
    margin: 0;
    span {
      font-weight: 700;
    }
  }
}
</style>
