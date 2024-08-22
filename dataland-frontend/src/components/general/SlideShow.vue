<template>
  <div @pointerdown="dragStartCondition" @touchstart="dragStartCondition" :class="slidesWrapperClasses">
    <div ref="slider" role="list" :class="slidesContainerClasses">
      <slot />
    </div>
  </div>
  <div v-if="slideCount > 1" :class="arrowsContainerClasses">
    <button @click="move(-1)" aria-label="Previous slide" :class="[leftArrowClasses, { disabled: disableLeftArrow }]" />
    <button @click="move(1)" aria-label="Next slide" :class="[rightArrowClasses, { disabled: disableRightArrow }]" />
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref, toRefs, computed } from 'vue';

const props = withDefaults(
  defineProps<{
    slidesWrapperClasses: string;
    slidesContainerClasses: string;
    arrowsContainerClasses: string;
    leftArrowClasses: string;
    rightArrowClasses: string;
    slideCount: number;
    initialCenterSlide?: number;
    scrollScreenWidthLimit?: number;
    slideWidth: number;
  }>(),
  {
    initialCenterSlide: 0,
    slideWidth: 440,
  }
);

const { slideCount, initialCenterSlide, scrollScreenWidthLimit, slideWidth } = toRefs(props);

const slider = ref<HTMLElement | null>(null);
const currentSlide = ref(0);
const emit = defineEmits(['update:currentSlide']);

const state = {
  isDragging: false,
  startPos: 0,
  currentTranslate: 0,
  prevTranslate: 0,
  disableScroll: false,
  thresholdReached: false,
  startX: 0,
  startY: 0,
};

const preventDefault: EventListener = (e: Event) => {
  if (e.cancelable) {
    e.preventDefault();
  }
};

const disableLeftArrow = computed(() => {
  return currentSlide.value <= 0 - initialCenterSlide.value;
});

const disableRightArrow = computed(() => {
  return currentSlide.value >= slideCount.value - 1 - initialCenterSlide.value;
});

const toggleScrollLock = (lock: boolean): void => {
  if (lock) {
    document.addEventListener('touchmove', preventDefault, { passive: false } as EventListenerOptions);
  } else {
    document.removeEventListener('touchmove', preventDefault, { passive: false } as EventListenerOptions);
  }
  state.disableScroll = lock;
};

const dragStartCondition = (e: PointerEvent | TouchEvent): void => {
  if (slideCount.value <= 1) return;
  state.startX = 'touches' in e ? e.touches[0].pageX : e.pageX;
  state.startY = 'touches' in e ? e.touches[0].pageY : e.pageY;
  dragStart(e);
};

const setSliderPosition = (sliderElement: HTMLElement, animate = true): void => {
  if (animate) sliderElement.style.transition = 'transform 0.3s ease-out';
  sliderElement.style.transform = `translate3d(${state.currentTranslate}px, 0, 0)`;
};

const move = (direction: number): void => {
  if (direction === 1 && currentSlide.value < slideCount.value - 1 - initialCenterSlide.value) currentSlide.value++;
  if (direction === -1 && currentSlide.value > 0 - initialCenterSlide.value) currentSlide.value--;

  emit('update:currentSlide', currentSlide.value);

  state.currentTranslate = currentSlide.value * -slideWidth.value;
  if (slider.value) setSliderPosition(slider.value);
};

const dragStart = (e: PointerEvent | TouchEvent): void => {
  if (scrollScreenWidthLimit?.value && window.innerWidth > scrollScreenWidthLimit.value) {
    return;
  }
  state.isDragging = true;
  state.startPos = 'touches' in e ? e.touches[0].pageX : e.pageX;
  state.prevTranslate = state.currentTranslate;

  if (slider.value) {
    slider.value.style.transition = 'none';
    slider.value.classList.add('isdragging');
  }

  document.addEventListener('pointermove', drag);
  document.addEventListener('pointerup', dragEnd);
  document.addEventListener('touchmove', drag);
  document.addEventListener('touchend', dragEnd);
};

const drag = (e: PointerEvent | TouchEvent): void => {
  if (!state.isDragging) return;

  const { pageX } = 'touches' in e ? e.touches[0] : e;
  const dx = Math.abs(pageX - state.startX);
  state.thresholdReached = dx > 20;
  if (state.thresholdReached && !state.disableScroll) {
    toggleScrollLock(true);
  } else if (!state.thresholdReached && state.disableScroll) {
    toggleScrollLock(false);
  }

  state.currentTranslate = state.prevTranslate + pageX - state.startPos;

  if (slider.value) setSliderPosition(slider.value, false);
};

const dragEnd = (): void => {
  state.isDragging = false;
  if (state.disableScroll) toggleScrollLock(false);

  const movedBy = state.currentTranslate - state.prevTranslate;
  if (movedBy < -100 && currentSlide.value < slideCount.value - 1 - initialCenterSlide.value) currentSlide.value++;
  if (movedBy > 100 && currentSlide.value > 0 - initialCenterSlide.value) currentSlide.value--;

  emit('update:currentSlide', currentSlide.value);

  state.currentTranslate = currentSlide.value * -slideWidth.value;

  if (slider.value) {
    setSliderPosition(slider.value);
    slider.value.classList.remove('isdragging');
  }

  document.removeEventListener('pointermove', drag);
  document.removeEventListener('pointerup', dragEnd);
  document.removeEventListener('touchmove', drag);
  document.removeEventListener('touchend', dragEnd);
};

onUnmounted(() => {
  document.removeEventListener('pointermove', drag);
  document.removeEventListener('pointerup', dragEnd);
  document.removeEventListener('touchmove', drag);
  document.removeEventListener('touchend', dragEnd);
});
</script>
