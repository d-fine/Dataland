<template>
  <button
    class="joincampaign__button"
    @click="() => openEmailClient(getInTouchSection?.cards?.[2])"
    aria-label="Indicate interest by opening email client"
    role="button"
  >
    I AM INTERESTED
  </button>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";
import { openEmailClient } from "@/utils/Email";

const { sections } = defineProps<{ sections?: Section[] }>();

const getInTouchSection = computed(() => {
  return sections?.find((section) => section.title === "Get in touch") ?? null;
});
</script>
