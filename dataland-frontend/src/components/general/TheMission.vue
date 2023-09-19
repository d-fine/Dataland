<template>
  <section v-if="brandsSection" class="mission" role="region" aria-label="Mission Statement">
    <div class="mission__wrapper">
      <h2 id="mission-heading" aria-labelledby="mission-heading">
        <template v-for="(part, index) in brandsSection.text" :key="index">
          <span v-if="index === 1" role="emphasis">{{ part }}</span>
          <template v-else>{{ part }}</template>
        </template>
      </h2>
      <div class="mission__tiles" role="list">
        <div
          v-for="(card, index) in brandsSection.cards"
          :key="index"
          class="mission__tile"
          :class="`mission__tile--type_${index + 1}`"
          role="listitem"
        >
          <img :src="card.icon" :alt="card.text" class="mission__tile__icon" />
          <p class="mission__tile__text">{{ card.text }}</p>
        </div>
      </div>
      <router-link
        to="/mission"
        class="mission__button mission__button--black"
        aria-label="Read More About Our Mission"
      >
        OUR MISSION
      </router-link>
    </div>
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
  align-items: center;
  &__wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    max-width: 1288px;
    width: 100%;
    gap: 113px;
  }
  h2 {
    font-size: 160px;
    font-style: normal;
    font-weight: 600;
    line-height: 160px; /* 100% */
    max-width: 1017px;
    margin: 0;
    span {
      color: #ff6813;
    }
  }

  &__tiles {
    display: flex;
    justify-content: space-between;
    width: 100%;
    gap: 32px;
  }

  &__tile {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 528px;
    padding-top: 80px;
    background-color: var(--default-neutral-white);
    border-radius: 16px;
    gap: 32px;
    transition:
      background-color 0.3s ease,
      border-radius 0.3s ease;
    cursor: pointer;

    &__icon {
      width: 160px;
    }
    &__text {
      font-size: 48px;
      font-style: normal;
      font-weight: 600;
      line-height: 56px; /* 116.667% */
      letter-spacing: 0.25px;
      text-align: center;
      max-width: 308px;
      transition: color 0.3s ease;
      .mission__tile:hover & {
        color: var(--primary-orange);
      }
    }
    &:hover {
      background-color: #ffebe0;
      border-radius: 32px;
    }
  }
  &__button {
    display: inline-block;
    padding: 14px 32px;
    font-size: 16px;
    font-style: normal;
    font-weight: 600;
    line-height: 24px; /* 150% */
    letter-spacing: 0.75px;
    text-transform: uppercase;
    border-radius: 32px;
    text-align: center;
    cursor: pointer;
    text-decoration: none;
    &--black {
      background-color: #1b1b1b;
      border: 2px solid #1b1b1b;
      color: white;
    }

    &:hover {
      background-color: transparent;
      color: #1b1b1b;
    }
  }
}
</style>
