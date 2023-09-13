<template>
  <section class="howitworks" role="region" aria-labelledby="howitworks-heading">
    <div class="howitworks__wrapper">
      <h2 id="howitworks-heading" class="howitworks__title">
        {{ sectionText }}
      </h2>
      <div ref="slider" role="list" class="howitworks__slides" @pointerdown="dragStart" @touchstart="dragStart">
        <div v-for="(slide, index) in slides" :key="index" role="listitem" class="howitworks__slide">
          <h3 class="howitworks__slide-title">{{ slide.title }}</h3>
          <p class="howitworks__slide-text">{{ slide.text }}</p>
          <p class="howitworks__slide-index">0{{ index + 1 }}</p>
        </div>
      </div>
      <div class="howitworks__arrows">
        <button
          @click="move(-1)"
          aria-label="Previous slide"
          class="howitworks__arrow howitworks__arrow--left"
        ></button>
        <button @click="move(1)" aria-label="Next slide" class="howitworks__arrow howitworks__arrow--right"></button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();
const howItWorksSection = computed(() => sections?.find((s) => s.title === "How it works"));
const sectionText = computed(() => howItWorksSection.value?.text.join(" ") || "");
const slides = computed(() => sections?.find((s) => s.title === "How it works")?.cards || []);
const slider = ref<HTMLElement | null>(null);
const currentSlide = ref(0);

let isDragging = false;
let startPos = 0;
let currentTranslate = 0;
let prevTranslate = 0;

const setSliderPosition = (sliderElement: HTMLElement, animate = true): void => {
  if (animate) sliderElement.style.transition = "transform 0.3s ease-out";
  sliderElement.style.transform = `translate3d(${currentTranslate}px, 0, 0)`;
};

const move = (direction: number): void => {
  const slideCount = slides.value.length;
  if (direction === 1 && currentSlide.value < slideCount - 1) currentSlide.value++;
  if (direction === -1 && currentSlide.value > 0) currentSlide.value--;

  currentTranslate = currentSlide.value * -440;
  if (slider.value) setSliderPosition(slider.value);
};

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
  if (movedBy < -100 && currentSlide.value < slides.value.length - 1) currentSlide.value++;
  if (movedBy > 100 && currentSlide.value > 0) currentSlide.value--;

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

<style lang="scss">
.howitworks {
  padding: 120px 0 64px 296px;
  &__wrapper {
    position: relative;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    gap: 56px;
  }
  &__title {
    font-size: 64px;
    font-style: normal;
    font-weight: 700;
    line-height: 78px;
    margin: 0;
  }

  &__slides {
    display: flex;
    transition: transform 0.3s ease-out;
    gap: 32px;
    &.isdragging .howitworks__slide {
      cursor: grabbing;
    }
  }

  &__slide {
    flex: 0 0 408px;
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
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='47' height='47' viewBox='0 0 47 47' fill='none'%3E%3Crect width='46' height='46' transform='translate(0.666992 0.315918)' fill='white' fill-opacity='0.01' style='mix-blend-mode:multiply'/%3E%3Cpath d='M26.542 8.94092L24.4864 10.9434L35.3826 21.8784H6.41699V24.7534H35.3826L24.4864 35.6396L26.542 37.6909L40.917 23.3159L26.542 8.94092Z' fill='%23161616'/%3E%3C/svg%3E");
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
}
</style>
