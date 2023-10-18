<template>
  <section v-if="quotesSection" class="quotes" role="region" aria-label="The Quotes">
    <SlideShow
      slides-wrapper-classes="quotes__sliderwrapper"
      slides-container-classes="quotes__slides"
      arrows-container-classes="quotes__arrows"
      left-arrow-classes="quotes__arrow quotes__arrow--left"
      right-arrow-classes="quotes__arrow quotes__arrow--right"
      :slide-count="slides.length"
      :initial-center-slide="1"
      @update:currentSlide="(newSlide) => (currentSlide = newSlide)"
      :scroll-screen-width-limit="1800"
      :slide-width="323"
    >
      <div v-for="(card, index) in cards" :key="index" role="listitem" class="quotes__slide">
        <div class="quotes__slide-videoContainer">
          <iframe
            :src="'https://www.youtube.com/embed/' + card.icon + '?rel=0'"
            title="Youtube video player"
            allowfullscreen
            class="quotes__slide-video"
          ></iframe>
        </div>
      </div>
    </SlideShow>
    <p class="quotes__slide-text">{{ currentCardInfo.date }}</p>
    <h3>
      {{ currentCardInfo.title }} <span>{{ currentCardInfo.text }}</span>
    </h3>
    <RegisterButton :buttonText="quotesSection.text[0]" />
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import type { Section } from "@/types/ContentTypes";
import RegisterButton from "@/components/resources/newLandingPage/RegisterButton.vue";
import SlideShow from "@/components/general/SlideShow.vue";

const { sections } = defineProps<{ sections?: Section[] }>();
const quotesSection = computed(() => sections?.find((s) => s.title === "Quotes"));
const cards = computed(() => quotesSection.value?.cards ?? []);
const slides = computed(() => sections?.find((s) => s.title === "Quotes")?.cards ?? []);
const currentSlide = ref(0);
const currentCardInfo = computed(() => {
  const card = cards.value[currentSlide.value + 1];
  return {
    date: card?.date,
    title: card?.title,
    text: card?.text,
  };
});
</script>

<style lang="scss">
.quotes {
  margin: 20px auto 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  &__slides {
    display: flex;
    transition: transform 0.3s ease-out;
    gap: 32px;
    justify-content: center;
    &.isdragging .howitworks__slide {
      cursor: grabbing;
    }
  }

  &__slide {
    flex: 0 0 323px;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    gap: 24px;
    cursor: grab;

    &-videoContainer {
      aspect-ratio: 16 / 9;
      width: 100%;
      overflow: hidden;
    }

    &-video {
      width: 100%;
      height: 100%;
      border-width: 0;
      border-radius: 8px;
    }

    &-title {
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 24px; /* 150% */
      letter-spacing: 0.25px;
      margin: 0;
      span {
        color: var(--primary-orange);
        display: block;
      }
    }
    &-text {
      font-size: 32px;
      font-style: normal;
      font-weight: 600;
      line-height: 40px; /* 125% */
      letter-spacing: 0.25px;
      margin-top: 36px;
      max-width: 666px;
    }
  }
  &__arrows {
    display: flex;
    gap: 18px;
  }
  &__arrow {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid rgba(203, 203, 203, 0.24);
    background-color: #fff;
    cursor: pointer;
    &:hover {
      border: 2px solid #585858;
    }

    &--left,
    &--right {
      &::before {
        content: "";
        display: block;
        width: 24px;
        height: 24px;
        background-image: url("/static/icons/Arrow--right.svg");
        background-size: contain;
        background-repeat: no-repeat;
      }
    }

    &--left {
      &::before {
        transform: scaleX(-1);
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
@media only screen and (max-width: $small) {
  .quotes {
    margin: 20px auto 80px;
    &__slide {
      &-text {
        font-size: 20px;
        line-height: 28px; /* 140% */
      }
    }
    &__button {
      margin: 64px 16px 0;
    }
  }
}
</style>
