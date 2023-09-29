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
    display: grid;
    grid-template-columns: repeat(16, 1fr);
    gap: 32px;
    max-width: 1440px;
    width: 100%;
    padding: 0 32px;
    margin: 140px auto 21px;
  }
  h2 {
    font-size: 160px;
    font-style: normal;
    font-weight: 600;
    line-height: 160px; /* 100% */
    margin: 0;
    grid-column: 3/15;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    span {
      color: var(--primary-orange);
    }
  }
}
@media only screen and (max-width: $large) {
  .claim {
    &__wrapper {
      margin-bottom: 0;
      h2 {
        font-size: 100px;
        font-style: normal;
        font-weight: 700;
        line-height: 106px; /* 106% */
        letter-spacing: 0.25px;
        margin-bottom: 0;
      }
    }
  }
}
</style>
