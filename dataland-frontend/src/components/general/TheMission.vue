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
    display: grid;
    grid-template-columns: repeat(16, 1fr);
    gap: 113px 32px;
    max-width: 1440px;
    width: 100%;
    padding: 0 32px;
  }
  h2 {
    grid-column: 3 / 15;
    font-size: 160px;
    font-style: normal;
    font-weight: 600;
    line-height: 160px; /* 100% */
    max-width: 1017px;
    margin: 0;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    span {
      color: var(--primary-orange);
    }
  }

  &__tiles {
    grid-column: 2 / 16;
    display: flex;
    justify-content: center; // centers the tiles
    gap: 32px;
    width: 100%;
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
    grid-column: span 16;
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
    width: fit-content;
    margin: 0 auto;
    cursor: pointer;
    text-decoration: none;
    &--black {
      background-color: var(--basic-dark);
      border: 2px solid var(--basic-dark);
      color: white;
    }

    &:hover {
      background-color: transparent;
      color: var(--basic-dark);
    }
  }
}
@media only screen and (max-width: $large) {
  .mission {
    h2 {
      font-size: 100px;
      font-weight: 700;
      line-height: 106px; /* 106% */
      letter-spacing: 0.25px;
      max-width: 809px;
    }
    &__text {
      font-size: 40px;
      font-weight: 600;
      line-height: 48px; /* 120% */
      letter-spacing: 0.25px;
    }
  }
}
</style>
