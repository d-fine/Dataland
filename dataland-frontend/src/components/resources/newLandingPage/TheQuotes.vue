<template>
  <section v-if="quotesSection" class="quotes" role="region" aria-label="The Quotes">
    <SlideShow
      slides-wrapper-classes="quotes__sliderwrapper"
      slides-container-classes="quotes__slides"
      arrows-container-classes="quotes__arrows"
      left-arrow-classes="quotes__arrow quotes__arrow--left"
      right-arrow-classes="quotes__arrow quotes__arrow--right"
      :slide-count="cards.length"
      :initial-center-slide="1"
      @update:currentSlide="(newSlide) => (currentSlide = newSlide)"
      :scroll-screen-width-limit="1800"
      :slide-width="slideWidth"
    >
      <div v-for="(card, index) in cards" :key="index" role="listitem" class="quotes__slide">
        <div class="quotes__slide-videoContainer">
          <iframe
            :src="'https://www.youtube.com/embed/' + card.icon + '?rel=0'"
            title="Youtube video player"
            allowfullscreen
            :class="{ 'quotes__slide-video--zoom-out': currentSlide !== index - 1, 'quotes__slide-video': true }"
          ></iframe>
        </div>
      </div>
    </SlideShow>
    <p class="quotes__slide-text">{{ currentCardInfo.date }}</p>
    <h3 class="quotes__slide-title">
      {{ currentCardInfo.title }} <span>{{ currentCardInfo.text }}</span>
    </h3>
    <RegisterButton :buttonText="quotesSection.text[0]" />
  </section>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from "vue";
import type { Section } from "@/types/ContentTypes";
import RegisterButton from "@/components/resources/newLandingPage/RegisterButton.vue";
import SlideShow from "@/components/general/SlideShow.vue";

const { sections } = defineProps<{ sections?: Section[] }>();
const quotesSection = computed(() => sections?.find((s) => s.title === "Quotes"));
const cards = computed(() => quotesSection.value?.cards ?? []);
const currentSlide = ref(0);
const currentCardInfo = computed(() => {
  const card = cards.value[currentSlide.value + 1];
  return {
    date: card?.date,
    title: card?.title,
    text: card?.text,
  };
});

const slideWidth = ref(760);

const updateSlideWidth = (): void => {
  slideWidth.value = window.innerWidth > 768 ? 760 : 323;
};
updateSlideWidth();

onMounted(() => {
  window.addEventListener("resize", updateSlideWidth);
  updateSlideWidth();
});

onUnmounted(() => {
  window.removeEventListener("resize", updateSlideWidth);
});
</script>

<style lang="scss">
.quotes {
  margin: 0 auto 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  gap: 40px;
  &__slides {
    display: flex;
    transition: transform 0.4s ease-out;
    gap: 0;
    justify-content: center;
    &.isdragging .howitworks__slide {
      cursor: grabbing;
    }
  }

  &__slide {
    flex: 0 0 760px;
    -webkit-flex: 0 0 760px;
    -ms-flex: 0 0 760px;
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
      -webkit-border-radius: 8px;
      -moz-border-radius: 8px;
      transition: transform 0.4s ease-in-out;
      &--zoom-out {
        -ms-transform: scale(0.765);
        -webkit-transform: scale(0.765);
        transform: scale(0.765);
      }
    }

    &-title {
      font-size: 14px;
      font-style: normal;
      font-weight: 600;
      line-height: 20px;
      letter-spacing: 0.25px;
      margin: 0;
      span {
        color: var(--primary-orange);
        display: block;
      }
    }
    &-text {
      font-size: 24px;
      font-style: normal;
      font-weight: 600;
      line-height: 32px; /* 133.333% */
      letter-spacing: 0.25px;
      max-width: 470px;
      margin: 0 16px;
    }
  }
  &__arrows {
    display: flex;
    gap: 18px;
    touch-action: manipulation;
    -webkit-touch-action: manipulation;
    -ms-touch-action: manipulation;
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
        -webkit-transform: scaleX(-1);
        -moz-transform: scaleX(-1);
        -ms-transform: scaleX(-1);
        -o-transform: scaleX(-1);
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
    margin: 32px auto 64px;
    gap: 32px;
    &__slide {
      flex: 0 0 323px;

      &-text {
        font-size: 20px;
        line-height: 28px; /* 140% */
      }
    }
    &__arrows {
      order: 1;
    }
    &__button {
      display: none;
      margin: 32px 16px 0;
      order: 2;
    }
  }
}
</style>
