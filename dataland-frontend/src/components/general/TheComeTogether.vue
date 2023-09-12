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
            <span>{{ card.title }}</span>
          </div>
          <div role="gridcell" class="cometogether__cell">
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

const { sections } = defineProps<{ sections?: Section[] }>();

const comeTogetherSection = computed(() => {
  return sections?.find((section) => section.title === "Come together") || null;
});
</script>

<style scoped lang="scss">
.cometogether {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 140px 0 140px 296px;
  background-color: #f6f6f6;

  &__wrapper {
    display: flex;
    flex-direction: column;
    gap: 48px;
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
      color: #a5a5a5;
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    align-self: flex-end;
    margin: 0 120px 0 177px;
    gap: 32px;
  }
  &__row {
    background-color: #ffffff;
    display: flex;
    border-radius: 16px;
    gap: 16px;
    flex-direction: column;
    padding: 64px 40px;
    height: 380px;
  }
  &__cell {
    font-size: 20px;
    color: #5b5b5b;
    font-style: normal;
    font-weight: 400;
    line-height: 28px; /* 140% */
    letter-spacing: 0.25px;
    &--icon {
      color: #ff6813;
      font-size: 64px;
      font-weight: 700;
      line-height: 78px;
    }
  }

  //   span {
  //     font-weight: 700;
  //   }
  //   &:last-child:not(:last-of-type)::after {
  //     content: "";
  //     display: block;
  //     position: absolute;
  //     bottom: 0;
  //     left: 24px; // same as left padding
  //     right: 24px; // same as right padding
  //     height: 1px;
  //     background: #ccc;
  //   }
  //   &:last-child {
  //     justify-content: flex-end;
  //   }

  //   &--icon {
  //     font-size: 20px;

  //     font-weight: 600;
  //     line-height: 28px; /* 140% */
  //     letter-spacing: 0.25px;
  //     img {
  //       width: 24px;
  //       height: 24px;
  //       margin-right: 16px;
  //     }
  //   }
  // }

  // &__button {
  //   padding: 14px 32px;
  //   border-radius: 32px;
  //   background-color: #ff6813;
  //   color: #fff;
  //   font-size: 16px;
  //   font-style: normal;
  //   font-weight: 600;
  //   line-height: 20px;
  //   letter-spacing: 0.75px;
  //   text-transform: uppercase;
  //   border: 2px solid #ff6813;
  //   cursor: pointer;
  //   cursor: pointer;
  // }
}
</style>
