<template>
  <button
    class="joincampaign__button"
    @click="openEmailClient"
    aria-label="Indicate interest by opening email client"
    role="button"
  >
    I AM INTERESTED
  </button>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const getInTouchSection = computed(() => {
  return sections?.find((section) => section.title === "Get in touch") || null;
});

const openEmailClient = (): void => {
  if (getInTouchSection.value && getInTouchSection.value.cards) {
    const cards = getInTouchSection.value.cards;

    const email = cards[3]?.icon || "";
    const subject = cards[3]?.title || "";
    const body = cards[3]?.text || "";

    if (email && subject && body) {
      const mailtoString = `mailto:${email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
      window.location.href = mailtoString;
    }
  }
};
</script>
