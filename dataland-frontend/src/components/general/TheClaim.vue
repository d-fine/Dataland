<template>
  <section v-if="claimSection" class="claim" role="region" aria-label="Claim Statement">
    <div class="claim__wrapper">
      <h2 class="claim__title">
        <template v-for="(part, index) in claimSection.text" :key="index">
          <span v-if="index === 1" role="emphasis">{{ part }}</span>
          <template v-else>{{ part }}</template>
        </template>
      </h2>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const claimSection = computed(() => {
  return sections?.find((section) => section.title === "Claim") ?? null;
});
</script>

<style scoped lang="scss">
.claim {
  display: flex;
  justify-content: center;
  align-items: center;
  &__wrapper {
    width: 100%;
    max-width: 1109px;
    margin: 140px auto 21px;
  }
  h2 {
    font-size: 160px;
    font-style: normal;
    font-weight: 600;
    line-height: 160px; /* 100% */
    margin: 0 108px;

    span {
      color: #ff6813;
    }
  }
}
</style>
