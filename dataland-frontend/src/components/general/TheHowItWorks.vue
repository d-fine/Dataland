<template>
  <section class="howitworks">
    <button @click="move(-1)" class="howitworks__arrow howitworks__arrow--left">←</button>
    <div
      ref="slider"
      class="howitworks__slides"
      @mousedown="dragStart"
      @mousemove="drag"
      @mouseup="dragEnd"
      @mouseleave="dragEnd"
      @touchstart="dragStart"
      @touchmove="drag"
      @touchend="dragEnd"
    >
      <div v-for="slide in slides" :key="slide" class="howitworks__slide">
        <p>{{ slide }}</p>
      </div>
    </div>
    <button @click="move(1)" class="howitworks__arrow howitworks__arrow--right">→</button>
  </section>
</template>

<script setup lang="ts">
import { type Ref, ref } from "vue";

// Explicitly define the type for slider ref
const slider: Ref<HTMLElement | null> = ref(null);

const slides = Array.from({ length: 10 }, (_, i) => `Slide ${i + 1}`);
const currentSlide = ref(0);

let isDragging = false;
let startPos = 0;
let currentTranslate = 0;
let prevTranslate = 0;

const setSliderPosition = (animate = true): void => {
  if (slider.value) {
    if (animate) slider.value.style.transition = "transform 0.3s ease-out";
    currentTranslate = currentSlide.value * -300;
    slider.value.style.transform = `translate3d(${currentTranslate}px, 0, 0)`;
  }
};

const move = (direction: number): void => {
  if (direction === 1 && currentSlide.value < slides.length - 1) currentSlide.value++;
  if (direction === -1 && currentSlide.value > 0) currentSlide.value--;
  setSliderPosition();
};

const dragStart = (e: MouseEvent | TouchEvent): void => {
  isDragging = true;
  if ("pageX" in e) {
    startPos = e.pageX;
  } else {
    startPos = e.touches[0].pageX;
  }
  prevTranslate = currentTranslate;
  if (slider.value) slider.value.style.transition = "none";
};

const drag = (e: MouseEvent | TouchEvent): void => {
  if (!isDragging) return;
  let currentPos: number;

  if ("pageX" in e) {
    currentPos = e.pageX;
  } else {
    currentPos = e.touches[0].pageX;
  }

  currentTranslate = prevTranslate + currentPos - startPos;

  if (slider.value) {
    slider.value.style.transform = `translate3d(${currentTranslate}px, 0, 0)`;
  }
};

const dragEnd = (): void => {
  isDragging = false;
  const movedBy = currentTranslate - prevTranslate;
  if (movedBy < -100 && currentSlide.value < slides.length - 1) currentSlide.value++;
  if (movedBy > 100 && currentSlide.value > 0) currentSlide.value--;
  setSliderPosition();
};
</script>

<style lang="scss">
.howitworks {
  position: relative;
  overflow: hidden;
  width: 1200px;
  padding: 120px 0 64px;

  &__slides {
    display: flex;
    transition: transform 0.3s ease-out;
    gap: 20px;
  }

  &__slide {
    flex: 0 0 300px;
    border: 1px solid #ccc;
    border-radius: 5px;
  }

  &__arrow {
    position: absolute;
    top: 50%;
    z-index: 2;
    cursor: pointer;
    &--left {
      left: 10px;
    }
    &--right {
      right: 10px;
    }
  }
}
</style>
