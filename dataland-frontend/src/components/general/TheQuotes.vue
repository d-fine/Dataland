<template>
  <section v-if="quotesSection" class="quotes" role="region" aria-label="The Quotes">
    <div ref="slider" role="list" class="quotes__slides" @pointerdown="dragStart" @touchstart="dragStart">
      <div v-for="(card, index) in cards" :key="index" role="listitem" class="quotes__slide">
        <img :src="card.icon" class="quotes__slide-icon" />
        <h3 class="quotes__slide-title">{{ card.title }}</h3>
        <p class="quotes__slide-text">{{ card.text }}</p>
        <p class="quotes__slide-index">0{{ index + 1 }}</p>
      </div>
    </div>
    <div class="quotes__arrows">
      <button @click="move(-1)" aria-label="Previous slide" class="quotes__arrow quotes__arrow--left"></button>
      <button @click="move(1)" aria-label="Next slide" class="quotes__arrow quotes__arrow--right"></button>
    </div>
    <register-button :buttonText="quotesSection.text[0]" />
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from "vue";
import type { Section } from "@/types/ContentTypes";
import RegisterButton from "@/components/resources/newLandingPage/RegisterButton.vue";

const { sections } = defineProps<{ sections?: Section[] }>();
const quotesSection = computed(() => sections?.find((s) => s.title === "Quotes"));
const cards = computed(() => quotesSection.value?.cards || []);

const slides = computed(() => sections?.find((s) => s.title === "Quotes")?.cards || []);
const slider = ref<HTMLElement | null>(null);
const currentSlide = ref(0);

let isDragging = false;
let startPos = 0;
let currentTranslate = 0;
let prevTranslate = 0;

const setSliderPosition = (sliderElement: HTMLElement, animate = true): void => {
  if (animate) sliderElement.style.transition = "transform 0.3s ease-out";
  sliderElement.style.transform = `translate3d(${currentTranslate}px, 0, 0)`;
  console.log(`Slider is at translate3d(${currentTranslate}px, 0, 0)`);
};

const move = (direction: number): void => {
  const slideCount = slides.value.length - 1;
  console.log(`Current Slide: ${currentSlide.value}, Direction: ${direction}`);

  if (direction === 1 && currentSlide.value < slideCount - 1) currentSlide.value++;
  if (direction === -1 && currentSlide.value >= 0) currentSlide.value--;

  currentTranslate = currentSlide.value * -440;
  if (slider.value) setSliderPosition(slider.value);
};
// const goToSlide = (index: number): void => {
//   currentSlide.value = index - 1;
//   currentTranslate = currentSlide.value * -440;
//   if (slider.value) setSliderPosition(slider.value);
// };

const dragStart = (e: PointerEvent | TouchEvent): void => {
  isDragging = true;
  startPos = "touches" in e ? e.touches[0].pageX : e.pageX;

  prevTranslate = currentTranslate;

  if (slider.value) {
    slider.value.style.transition = "none";
    slider.value.classList.add("isdragging");
  }

  document.addEventListener("pointermove", drag);
  document.addEventListener("pointerup", dragEnd);
  document.addEventListener("touchmove", drag);
  document.addEventListener("touchend", dragEnd);
};

const drag = (e: PointerEvent | TouchEvent): void => {
  if (!isDragging) return;
  const currentPos = "touches" in e ? e.touches[0].pageX : e.pageX;

  currentTranslate = prevTranslate + currentPos - startPos;

  if (slider.value) {
    setSliderPosition(slider.value, false);
  }
};

const dragEnd = (): void => {
  isDragging = false;

  const movedBy = currentTranslate - prevTranslate;
  if (movedBy < -100 && currentSlide.value < slides.value.length - 2) currentSlide.value++;
  if (movedBy > 100 && currentSlide.value >= 0) currentSlide.value--;

  // Set currentTranslate based on the new slide index
  currentTranslate = currentSlide.value * -440;

  if (slider.value) {
    setSliderPosition(slider.value);
    slider.value.classList.remove("isdragging");
  }

  document.removeEventListener("pointermove", drag);
  document.removeEventListener("pointerup", dragEnd);
  document.removeEventListener("touchmove", drag);
  document.removeEventListener("touchend", dragEnd);
};

onUnmounted(() => {
  document.removeEventListener("pointermove", drag);
  document.removeEventListener("pointerup", dragEnd);
  document.removeEventListener("touchmove", drag);
  document.removeEventListener("touchend", dragEnd);
});
</script>

<style scoped lang="scss">
.quotes {
  margin: calc(64px + 120px) auto 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 64px;
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
    flex: 0 0 440px;
    border-radius: 16px;
    display: flex;
    padding: 64px 40px 32px 40px;
    flex-direction: column;
    background: #f6f6f6;
    gap: 24px;
    cursor: grab;

    &-title {
      font-size: 48px;
      font-style: normal;
      font-weight: 600;
      line-height: 56px; /* 116.667% */
      letter-spacing: 0.25px;
      margin: 0;
    }
    &-text {
      font-size: 20px;
      font-style: normal;
      font-weight: 400;
      line-height: 28px; /* 140% */
      letter-spacing: 0.25px;
      color: #585858;
    }
    &-index {
      margin: auto 0 0;
      font-size: 48px;
      font-style: normal;
      font-weight: 600;
      line-height: 56px; /* 116.667% */
      letter-spacing: 0.25px;
      color: #ff5c00;
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
    margin-top: 64px; //spacing
    &:hover {
      background-color: var(--default-neutral-white);
      color: var(--basic-dark);
    }
  }
}
@media only screen and (max-width: $large) {
}
</style>
