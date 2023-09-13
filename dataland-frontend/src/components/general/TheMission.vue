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
    max-width: 1200px;
    width: 100%;
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

  &__tiles {
    display: flex;
    justify-content: space-between;
    width: 100%;
    margin: 168px 0;
    gap: 32px;
  }

  &__tile {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: flex-start;
    height: 400px;
    padding: 40px;
    background-color: #f6f6f6;
    border-radius: 16px;

    &__icon {
      align-self: flex-start;
      width: 112px;
      height: 112px;
    }
    &__text {
      align-self: flex-start;
      margin: auto 0 0;
      font-size: 32px;
      font-style: normal;
      font-weight: 600;
      line-height: 44px; /* 137.5% */
      letter-spacing: 0.25px;
      text-align: left;
      position: relative;
      width: 100%;
      white-space: pre-wrap;
      &::after {
        content: "";
        position: absolute;
        right: 0;
        bottom: 0;
        width: 46px;
        height: 46px;
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='47' height='47' viewBox='0 0 47 47' fill='none'%3E%3Crect width='46' height='46' transform='translate(0.666992 0.315918)' fill='white' fill-opacity='0.01' style='mix-blend-mode:multiply'/%3E%3Cpath d='M26.542 8.94092L24.4864 10.9434L35.3826 21.8784H6.41699V24.7534H35.3826L24.4864 35.6396L26.542 37.6909L40.917 23.3159L26.542 8.94092Z' fill='%23161616'/%3E%3C/svg%3E");
        background-size: contain; // Ensure the background scales correctly
        background-repeat: no-repeat;
      }
    }
    &--type_3 {
      background-color: #ff6813;
      & .mission__tile__text {
        color: #fff;
        font-size: 24px;
        font-weight: 400;
        line-height: 32px; /* 133.333% */
        &::after {
          content: none;
        }
      }
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
