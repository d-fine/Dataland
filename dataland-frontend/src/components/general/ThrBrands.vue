<template>
  <section v-if="brandsSection" class="brands" role="region" aria-label="Introduction">
    <div class="brands__wrap">
      <p class="brands__text">{{ brandsSection.text }}</p>

      <!-- <img :src="brandsSection.image[0]" :alt="brandsSection.text" class="intro__img" /> -->
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
  padding: 64px 0;
  background-color: #f6f6f6;
}
</style>
