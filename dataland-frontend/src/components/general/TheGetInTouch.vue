<template>
  <section
    v-if="getInTouchSection && getInTouchSection.cards"
    class="getintouch"
    role="region"
    aria-labelledby="getintouch-heading"
  >
    <div class="getintouch__wrapper">
      <h2 id="getintouch-heading" class="getintouch__title">
        {{ getInTouchSection.text[0] }}
        <span v-if="getInTouchSection.text[1]"> {{ getInTouchSection.text[1] }}</span>
      </h2>
      <div v-if="getInTouchSection.cards[1]" class="getintouch__image" role="img" aria-label="Contact Image">
        <img :src="getInTouchSection.cards[1].icon" :alt="getInTouchSection.cards[1].title" />
      </div>
      <div class="getintouch__text" grid-column="15 / 17">
        <p class="getintouch__text-title">{{ getInTouchSection.cards[1].title }}</p>
        <p class="getintouch__text-text">{{ getInTouchSection.cards[1].text }}</p>
        <button class="getintouch__text-button">{{ getInTouchSection.text[0] }}</button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const getInTouchSection = computed(() => {
  return sections?.find((section) => section.title === "Get in touch") || null;
});
</script>

<style scoped lang="scss">
.getintouch {
  padding: 140px 0 200px;
  align-items: center;
  display: flex;
  flex-direction: column;
  background-color: var(--basic-dark);

  &__wrapper {
    display: grid;
    grid-template-columns: repeat(16, 1fr);
    gap: 48px 32px;
    max-width: 1440px;
    padding: 0 32px;
  }

  &__title {
    font-size: 64px;
    font-weight: 700;
    line-height: 78px;
    grid-column: 5 / 10;
    color: var(--default-neutral-white);
    text-align: left;
    margin: 0;
  }

  &__image {
    grid-column: 5 / 8;
    img {
      height: 320px;
      border-radius: 16px;
    }
  }

  &__text {
    grid-column: 9 / 14;
    text-align: left;
    font-size: 22.123px;
    font-style: normal;
    font-weight: 600;
    line-height: 33.185px; /* 150% */
    letter-spacing: 0.346px;
    color: var(--default-neutral-white);
    display: flex;
    flex-direction: column;
    justify-content: center;
    p {
      margin: 0;
    }
    &-text {
      color: var(--primary-orange);
    }
    &-button {
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
      margin-top: 32px;
      width: fit-content;
      &:hover {
        background-color: var(--default-neutral-white);
        color: var(--basic-dark);
      }
    }
  }
}
</style>
