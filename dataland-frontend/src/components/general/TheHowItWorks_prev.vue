<template>
  <section class="howitworks">
    <button @click="move(-1)" class="howitworks__arrow howitworks__arrow--left">←</button>
    <div ref="slider" class="howitworks__slides" @mousedown="dragStart" @touchstart="dragStart">
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
import { type Ref, ref, onUnmounted, computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const howItWorksSection = computed(() => {
  return sections?.find((section) => section.title === "How it works") || null;
});

// Explicitly define the type for slider ref
const slider: Ref<HTMLElement | null> = ref(null);

const slides = computed(() => howItWorksSection.value?.cards || []);
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
  const slideCount = slides.value?.length ?? 0;
  if (direction === 1 && currentSlide.value < slideCount - 1) currentSlide.value++;
  if (direction === -1 && currentSlide.value > 0) currentSlide.value--;
  setSliderPosition();
};

const dragEndDocument = (): void => {
  dragEnd();
  document.removeEventListener("mousemove", drag);
  document.removeEventListener("mouseup", dragEndDocument);
  document.removeEventListener("touchmove", drag);
  document.removeEventListener("touchend", dragEndDocument);
  document.removeEventListener("mouseleave", dragEndDocument);
};

const dragStart = (e: MouseEvent | TouchEvent): void => {
  console.log("dragStart", Date.now());

  isDragging = true;
  if ("pageX" in e) {
    startPos = e.pageX;
  } else {
    startPos = e.touches[0].pageX;
  }
  prevTranslate = currentTranslate;
  if (slider.value) slider.value.style.transition = "none";

  document.addEventListener("mousemove", drag);
  document.addEventListener("mouseup", dragEndDocument);
  document.addEventListener("touchmove", drag);
  document.addEventListener("touchend", dragEndDocument);
  document.addEventListener("mouseleave", dragEndDocument);
};

let animationFrameId: number;

const drag = (e: MouseEvent | TouchEvent): void => {
  console.log(`drag ${e instanceof MouseEvent ? "mouse" : "touch"}`, Date.now());
  if (!isDragging) return;

  cancelAnimationFrame(animationFrameId);

  animationFrameId = requestAnimationFrame(() => {
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
  });
};

const dragEnd = (): void => {
  console.log("dragEnd", Date.now());

  if (!isDragging) return;

  const movedBy = currentTranslate - prevTranslate;

  if (slider.value) {
    slider.value.style.transition = "transform 0.1s ease-out"; // Snappier transition
  }

  if (movedBy < -100 && currentSlide.value < slides.value.length - 1) currentSlide.value++;
  if (movedBy > 100 && currentSlide.value > 0) currentSlide.value--;

  isDragging = false;
  startPos = 0;
  prevTranslate = currentTranslate = 0;

  setSliderPosition(false); // Keep the snappier transition
};

// const dragEnd = (): void => {

//   if (!isDragging) return;

//   console.log("dragEnd");
//   isDragging = false;

//   const movedBy = currentTranslate - prevTranslate;
//   const slideCount = slides.value?.length ?? 0;

//   if (movedBy < -100 && currentSlide.value < slideCount - 1) currentSlide.value++;
//   if (movedBy > 100 && currentSlide.value > 0) currentSlide.value--;

//   startPos = 0;
//   prevTranslate = currentTranslate = 0;

//   setSliderPosition(true);
// };

// const handleMouseLeave = (): void => {
//   if (isDragging) {
//     dragEnd();
//   }
// };

onUnmounted(() => {
  document.removeEventListener("mousemove", drag);
  document.removeEventListener("mouseup", dragEndDocument);
  document.removeEventListener("touchmove", drag);
  document.removeEventListener("touchend", dragEndDocument);
  document.removeEventListener("mouseleave", dragEndDocument);
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
