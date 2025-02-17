<template>
  <section v-if="joinCampaignSection" class="joincampaign" role="region" aria-labelledby="joincampaign-heading">
    <div class="joincampaign__wrapper">
      <h2 id="joincampaign-heading" class="joincampaign__title">
        {{ joinCampaignSection.text[0] }}
        <span v-if="joinCampaignSection.text[1]"> {{ joinCampaignSection.text[1] }}</span
        >{{ joinCampaignSection.text[2] }}
      </h2>
      <p class="joincampaign__paragraph" v-if="joinCampaignSection.text[3]">{{ joinCampaignSection.text[3] }}</p>
      <ButtonComponent
        label="I am interested"
        buttonType="button-component joincampaign__button"
        ariaLabel="Indicate interest by opening email client"
        @click="() => openEmailClient(getInTouchSection?.cards?.[2])"
      />

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
import { computed, type ComputedRef } from 'vue';
import type { Section } from '@/types/ContentTypes';
import { openEmailClient } from '@/utils/Email';
import ButtonComponent from '@/components/resources/newLandingPage/ButtonComponent.vue';

const { sections } = defineProps<{ sections?: Section[] }>();
const findSection = (title: string): ComputedRef<Section | null> => {
  return computed(() => sections?.find((section) => section.title === title) ?? null);
};
const joinCampaignSection = findSection('Join a campaign');
const getInTouchSection = findSection('Get in touch');
</script>

<style scoped lang="scss">
@use '@/assets/scss/newVariables';

.joincampaign {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140px 0;

  &__wrapper {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 61px 32px;
    max-width: 900px;
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
    grid-column: 1 / -1;
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
    grid-column: 1 / -1;
    text-align: left;
    margin: 0;
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
    grid-column: 1 / -1;
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
    grid-column: 1 / -1;
    margin-top: 79px;
    justify-content: center;
  }
  &__row {
    border-radius: 16px;
    border: 1px solid rgba(244, 244, 244, 0.33);
    background: var(--basic-white, #fff);
    box-shadow: 0 4px 32px 0 rgba(0, 0, 0, 0.08);
    display: flex;
    gap: 16px;
    flex-direction: column;
    padding: 46px 32px;
    text-align: left;
    min-width: 228px;
  }
  &__cell {
    color: var(--grey-tones-900);
    font-size: 16px;
    font-style: normal;
    font-weight: 400;
    line-height: 24px; /* 150% */
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

@media only screen and (max-width: newVariables.$medium) {
  .joincampaign {
    padding: 80px 0;
    &__wrapper {
      grid-template-columns: repeat(12, 1fr);
    }
    &__title {
      font-size: 64px;
      line-height: 72px;
      max-width: 551px;
    }
    &__grid {
      grid-template-columns: repeat(2, 2fr);
      gap: 61px 22px;
      grid-column: 1 / -1;
      margin-top: 0;
    }
    &__row {
      gap: 24px;
      padding: 46px 40px;
      min-height: 340px;
    }
  }
}
@media only screen and (max-width: newVariables.$small) {
  .joincampaign {
    padding: 64px 0;
    &__wrapper {
      gap: 32px 16px;
      padding: 0 16px;
    }
    &__title {
      font-size: 48px;
      line-height: 56px;
    }
    &__paragraph {
      font-size: 32px;
      line-height: 40px;
    }
    &__grid {
      grid-template-columns: 1fr;
      gap: 32px;
      grid-column: 1 / -1;
    }
    &__row {
      min-height: unset;
    }
  }
}
</style>
