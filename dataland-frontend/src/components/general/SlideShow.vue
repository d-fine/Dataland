<template>
  <div
    ref="slider"
    role="list"
    :class="slidesContainerClasses"
    @pointerdown="dragStartCondition"
    @touchstart="dragStartCondition"
  >
    <slot />
  </div>
  <div v-if="slideCount > 1" :class="arrowsContainerClasses">
    <button @click="move(-1)" aria-label="Previous slide" :class="leftArrowClasses" />
    <button @click="move(1)" aria-label="Next slide" :class="rightArrowClasses" />
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref, defineEmits, toRefs } from "vue";

const props = withDefaults(
  defineProps<{
    slidesContainerClasses: string;
    arrowsContainerClasses: string;
    leftArrowClasses: string;
    rightArrowClasses: string;
    slideCount: number;
    initialCenterSlide: number;
    scrollScreenWidthLimit: number;
    slideWidth: number;
  }>(),
  {
    initialCenterSlide: 0,
    slideWidth: 440,
  },
);

const { slideCount, initialCenterSlide, scrollScreenWidthLimit, slideWidth } = toRefs(props);

const slider = ref<HTMLElement | null>(null);
const currentSlide = ref(0);
const emit = defineEmits(["update:currentSlide"]);

let isDragging = false;
let startPos = 0;
let currentTranslate = 0;
let prevTranslate = 0;

const dragStartCondition = (e: PointerEvent | TouchEvent): void => {
  if (slideCount.value <= 1) return;
  dragStart(e);
};

const setSliderPosition = (sliderElement: HTMLElement, animate = true): void => {
  if (animate) sliderElement.style.transition = "transform 0.3s ease-out";
  sliderElement.style.transform = `translate3d(${currentTranslate}px, 0, 0)`;
};

const move = (direction: number): void => {
  if (direction === 1 && currentSlide.value < slideCount.value - 1 - initialCenterSlide.value) currentSlide.value++;
  if (direction === -1 && currentSlide.value > 0 - initialCenterSlide.value) currentSlide.value--;

  emit("update:currentSlide", currentSlide.value);

  currentTranslate = currentSlide.value * -slideWidth.value;
  if (slider.value) setSliderPosition(slider.value);
};

const dragStart = (e: PointerEvent | TouchEvent): void => {
  if (scrollScreenWidthLimit.value && window.innerWidth > scrollScreenWidthLimit.value) return;
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
  if (movedBy < -100 && currentSlide.value < slideCount.value - 1 - initialCenterSlide.value) currentSlide.value++;
  if (movedBy > 100 && currentSlide.value > 0 - initialCenterSlide.value) currentSlide.value--;

  emit("update:currentSlide", currentSlide.value);

  currentTranslate = currentSlide.value * -slideWidth.value;

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
