<template>
  <section v-if="comeTogetherSection" class="cometogether" role="region" aria-labelledby="cometogether-heading">
    <div class="cometogether__wrapper">
      <h2 id="cometogether-heading" class="cometogether__title">
        {{ comeTogetherSection.text[0] }}
        <span v-if="comeTogetherSection.text[1]"> {{ comeTogetherSection.text[1] }}</span>
      </h2>
      <div class="cometogether__grid" role="grid" aria-labelledby="cometogether-heading">
        <div v-for="(card, index) in comeTogetherSection.cards" :key="index" role="row" class="cometogether__row">
          <div class="cometogether__cell cometogether__cell--icon" role="gridcell">
            <img :src="card.icon" :alt="card.title" />
            <span>{{ card.title }}</span>
          </div>
          <div role="gridcell" class="cometogether__cell">
            {{ card.text }}
          </div>
        </div>
      </div>
      <div class="cometogether__cta">
        {{ comeTogetherSection?.image?.[0] ?? "" }}
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const comeTogetherSection = computed(() => {
  return sections?.find((section) => section.title === "Come together") || null;
});
</script>

<style scoped lang="scss">
.cometogether {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140px 0 80px;
  background-color: var(--basic-dark);

  &__wrapper {
    display: grid;
    grid-template-columns: repeat(16, 1fr);
    gap: 32px;
    max-width: 1440px;
    width: 100%;
    padding: 0 32px;
  }

  &__title {
    font-size: 64px;
    font-style: normal;
    font-weight: 700;
    line-height: 78px;
    text-align: left;
    margin: 0;
    color: var(--default-neutral-white);
    grid-column: 4 / 11;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    span {
      color: var(--grey-tones-400);
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
    border: 1px solid rgba(244, 244, 244, 0.33);
    background: #262626;
    display: flex;
    border-radius: 16px;
    gap: 16px;
    flex-direction: column;
    padding: 46px 40px;
    height: 369px;
    max-width: 234px;
  }
  &__cell {
    font-size: 20px;
    color: var(--grey-tones-400);
    font-style: normal;
    font-weight: 400;
    line-height: 28px; /* 140% */
    letter-spacing: 0.25px;
    &--icon {
      color: var(--default-neutral-white);
      font-size: 64px;
      font-weight: 700;
      line-height: 78px;
      img {
        display: block;
        width: 48px;
        margin-bottom: 16px;
      }
    }
  }
  &__cta {
    font-size: 48px;
    font-style: normal;
    font-weight: 600;
    line-height: 56px; /* 116.667% */
    letter-spacing: 0.25px;
    text-align: right;
    color: var(--primary-orange);
    grid-column: 4/16;
  }
}

@media only screen and (max-width: $large) {
  .cometogether {
    &__title {
      font-size: 48px;
      font-weight: 600;
      line-height: 56px; /* 116.667% */
      letter-spacing: 0.25px;
    }
    &__row {
      padding: 46px 24px;
    }
    &__cell {
      &--icon {
        font-size: 40px;
        font-weight: 700;
        line-height: 48px;
      }
    }
  }
}
</style>
