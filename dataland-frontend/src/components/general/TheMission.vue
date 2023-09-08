<template>
  <section v-if="brandsSection" class="mission" role="region" aria-label="Mission Statement">
    <h2 id="mission-heading" aria-labelledby="mission-heading">
      <template v-for="(part, index) in brandsSection.text" :key="index">
        <span v-if="index === 1" role="emphasis">{{ part }}</span>
        <template v-else>{{ part }}</template>
      </template>
    </h2>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const brandsSection = computed(() => {
  return sections?.find((section) => section.title === "Mission") || null;
});
</script>

<style scoped lang="scss">
.mission {
  display: flex;
  flex-direction: column;
  padding: 120px 0;
  gap: 168px;
  align-items: center;
  h2 {
    font-size: 160px;
    font-style: normal;
    font-weight: 600;
    line-height: 160px; /* 100% */
    max-width: 984px;
    span {
      color: #ff6813;
    }
  }
}
</style>
