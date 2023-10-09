<template>
  <section v-if="joinCampaignSection" class="joincampaign" role="region" aria-labelledby="joincampaign-heading">
    <div class="joincampaign__wrapper">
      <h2 id="joincampaign-heading" class="joincampaign__title">
        {{ joinCampaignSection.text[0] }}
        <span v-if="joinCampaignSection.text[1]"> {{ joinCampaignSection.text[1] }}</span
        >{{ joinCampaignSection.text[2] }}
      </h2>
      <p class="joincampaign__paragraph" v-if="joinCampaignSection.text[3]">{{ joinCampaignSection.text[3] }}</p>
      <email-button :sections="sections" />

      <div class="joincampaign__grid" role="grid" aria-labelledby="joincampaign-heading">
        <div v-for="(card, index) in joinCampaignSection.cards" :key="index" role="row" class="joincampaign__row">
          <div class="joincampaign__cell joincampaign__cell--icon" role="gridcell">
            <span>{{ card.title }}</span>
          </div>
          <div role="gridcell" class="joincampaign__cell">
            {{ card.text }}
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";
import EmailButton from "@/components/resources/newLandingPage/EmailButton.vue";

const { sections } = defineProps<{ sections?: Section[] }>();

const joinCampaignSection = computed(() => {
  return sections?.find((section) => section.title === "Join a campaign") ?? null;
});
</script>

<style scoped lang="scss">
.joincampaign {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140px 0 200px;

  &__wrapper {
    display: grid;
    grid-template-columns: repeat(16, 1fr);
    gap: 32px;
    max-width: 1440px;
    width: 100%;
    padding: 0 32px;
  }

  &__title {
    font-size: 100px;
    font-style: normal;
    font-weight: 700;
    line-height: 106px; /* 106% */
    letter-spacing: 0.25px;
    text-align: left;
    margin: 0;
    grid-column: 4 / 15;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    span {
      color: var(--primary-orange);
    }
  }

  &__paragraph {
    font-size: 40px;
    font-style: normal;
    font-weight: 600;
    line-height: 48px; /* 120% */
    letter-spacing: 0.25px;
    grid-column: 4 / 15;
    text-align: left;
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
    grid-column: 4/10;
    width: fit-content;
    &:hover {
      background-color: var(--default-neutral-white);
      color: var(--basic-dark);
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    align-self: flex-end;
    gap: 32px;
    grid-column: 4 / 16;
  }
  &__row {
    border-radius: 16px;
    border: 1px solid rgba(244, 244, 244, 0.33);
    background: var(--basic-white, #fff);
    box-shadow: 0px 4px 32px 0px rgba(0, 0, 0, 0.08);
    backdrop-filter: blur(16px);
    display: flex;
    gap: 16px;
    flex-direction: column;
    padding: 46px 40px;
    height: 309px;
    text-align: left;
  }
  &__cell {
    font-size: 20px;
    color: var(--grey-tones-400);
    font-style: normal;
    font-weight: 400;
    line-height: 28px; /* 140% */
    letter-spacing: 0.25px;
    &--icon {
      color: var(--basic-dark);
      font-size: 32px;
      font-style: normal;
      font-weight: 600;
      line-height: 40px; /* 125% */
      letter-spacing: 0.25px;
    }
  }
  &__join-link {
    margin-top: auto; // Push it to the bottom
    text-decoration: none;
    color: var(--primary-orange);
    font-size: 48px;
    font-style: normal;
    font-weight: 600;
    line-height: 56px; /* 116.667% */
    letter-spacing: 0.25px;
    text-align: right;
  }
}
</style>
