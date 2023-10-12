<template>
  <div ref="slider" role="list" :class="slidesContainerClasses" @pointerdown="dragStart" @touchstart="dragStart">
    <slot />
  </div>
  <div :class="arrowsContainerClasses">
    <button @click="move(-1)" aria-label="Previous slide" :class="leftArrowClasses" />
    <button @click="move(1)" aria-label="Next slide" :class="rightArrowClasses" />
  </div>
</template>

<script setup lang="ts">
import { onUnmounted } from "vue";
import { ref, watchEffect } from "vue";

const { slideCount, initialCenterSlide, scrollScreenWidthLimit } = defineProps<{
  slidesContainerClasses: string;
  arrowsContainerClasses: string;
  leftArrowClasses: string;
  rightArrowClasses: string;
  slideCount: number;
  initialCenterSlide: number; // TODO default to 0
  scrollScreenWidthLimit?: number;
}>();

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
  if (direction === 1 && currentSlide.value < slideCount - 1 - initialCenterSlide) currentSlide.value++;
  if (direction === -1 && currentSlide.value > 0 - initialCenterSlide) currentSlide.value--;

  currentTranslate = currentSlide.value * -440;
  if (slider.value) setSliderPosition(slider.value);
};

watchEffect(() => {
  if (scrollScreenWidthLimit) {
    const handleResize = (): void => {
      if (window.innerWidth > 1800) {
        currentSlide.value = 0;
        currentTranslate = 0;
        if (slider.value) setSliderPosition(slider.value);
      }
    };

    window.addEventListener("resize", handleResize);

    onUnmounted(() => {
      window.removeEventListener("resize", handleResize);
    });
  }
});

const dragStart = (e: PointerEvent | TouchEvent): void => {
  // Disable dragging for window width greater than 1800px, for example
  if (scrollScreenWidthLimit && window.innerWidth > scrollScreenWidthLimit) return;
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
  if (movedBy < -100 && currentSlide.value < slideCount - 1 - initialCenterSlide) currentSlide.value++;
  if (movedBy > 100 && currentSlide.value > 0 - initialCenterSlide) currentSlide.value--;

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

<style scoped></style>
