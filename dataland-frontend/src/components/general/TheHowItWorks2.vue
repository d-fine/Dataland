<template>
  <section class="howitworks">
    <button @click="move(-1)" class="howitworks__arrow howitworks__arrow--left">←</button>
    <div ref="slider" class="howitworks__slides" @pointerdown="dragStart">
      <div v-for="(slide, index) in slides" :key="index" class="howitworks__slide">
        <h3>{{ slide.title }}</h3>
        <p>{{ slide.text }}</p>
        <p>Item Number: {{ index }}</p>
      </div>
    </div>
    <button @click="move(1)" class="howitworks__arrow howitworks__arrow--right">→</button>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();
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

  currentTranslate = currentSlide.value * -300; // Update this line
  if (slider.value) setSliderPosition(slider.value);
};

const dragStart = (e: PointerEvent): void => {
  isDragging = true;
  startPos = e.pageX;
  prevTranslate = currentTranslate;

  if (slider.value) slider.value.style.transition = "none";

  document.addEventListener("pointermove", drag);
  document.addEventListener("pointerup", dragEnd);
};

const drag = (e: PointerEvent): void => {
  if (!isDragging) return;

  const currentPos = e.pageX;
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
  currentTranslate = currentSlide.value * -300;

  if (slider.value) setSliderPosition(slider.value);

  document.removeEventListener("pointermove", drag);
  document.removeEventListener("pointerup", dragEnd);
};

onUnmounted(() => {
  document.removeEventListener("pointermove", drag);
  document.removeEventListener("pointerup", dragEnd);
});
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
