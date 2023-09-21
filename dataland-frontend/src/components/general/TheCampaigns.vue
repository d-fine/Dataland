<template>
  <section v-if="campaignSection" class="campaigns" role="region" aria-labelledby="campaigns-heading">
    <div class="campaigns__wrapper">
      <h2 id="campaigns-heading" class="campaigns__title">
        {{ campaignSection.text[0] }}
        <span v-if="campaignSection.text[1]"> {{ campaignSection.text[1] }}</span>
      </h2>
      <div class="campaigns__grid" role="grid" aria-labelledby="campaigns-heading">
        <div v-for="(card, index) in campaignSection.cards" :key="index" role="row" class="campaigns__row">
          <div class="campaigns__cell campaigns__cell--icon" role="gridcell">
            <img :src="card.icon" :alt="card.title" />
            <span>{{ card.title }}</span>
          </div>
          <div role="gridcell" class="campaigns__cell">
            Companies:&nbsp;&nbsp;<span>{{ card.text }}</span>
          </div>
          <div role="gridcell" class="campaigns__cell">
            <div v-if="card.date && card.date !== 'Starting soon!'">
              Starting date: <span>{{ card.date }}</span>
            </div>
            <span v-else-if="card.date === 'Starting soon!'">Starting soon!</span>
          </div>
          <div role="gridcell" class="campaigns__cell">
            <button class="campaigns__button">JOIN</button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const campaignSection = computed(() => {
  return sections?.find((section) => section.title === "Campaigns") || null;
});
</script>

<style scoped lang="scss">
.campaigns {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140px 0 200px;

  &__wrapper {
    display: flex;
    flex-direction: column;
    gap: 80px;
  }

  &__title {
    font-size: 4rem;
    font-style: normal;
    font-weight: 700;
    line-height: 4.875rem; /* 121.875% */
    text-align: left;
    max-width: 672px;
    margin: 0;
    span {
      color: var(--grey-tones-400);
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    align-self: flex-end;
    border-radius: 16px;
    border: 1px solid rgba(192, 192, 192, 0.24);
    background: rgba(255, 255, 255, 0.72);
    /* Glass Effect */
    box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.08);
    backdrop-filter: blur(16px);
  }

  &__row {
    display: contents;
  }

  &__cell {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: normal;
    letter-spacing: 0.44px;
    white-space: nowrap;
    padding: 30px 24px;
    span {
      font-weight: 700;
    }
    &:last-child:not(:last-of-type)::after {
      content: "";
      display: block;
      position: absolute;
      bottom: 0;
      left: 24px; // same as left padding
      right: 24px; // same as right padding
      height: 1px;
      background: #ccc;
    }
    &:last-child {
      justify-content: flex-end;
    }

    &--icon {
      font-size: 20px;

      font-weight: 600;
      line-height: 28px; /* 140% */
      letter-spacing: 0.25px;
      img {
        width: 24px;
        height: 24px;
        margin-right: 16px;
      }
    }
  }

  &__button {
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
    &:hover {
      background-color: var(--default-neutral-white);
      color: var(--basic-dark);
    }
  }
}
</style>
