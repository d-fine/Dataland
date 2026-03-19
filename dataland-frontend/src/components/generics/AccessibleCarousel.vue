<template>
  <div class="accessible-carousel" role="region" :aria-label="ariaLabel" @keydown="handleKeydown">
    <div class="accessible-carousel__controls">
      <button
        v-if="autoScroll"
        class="accessible-carousel__pause-btn"
        :aria-label="isPaused ? 'Play carousel' : 'Pause carousel'"
        @click="togglePause"
      >
        <i :class="isPaused ? 'pi pi-play' : 'pi pi-pause'" />
      </button>
    </div>

    <div class="accessible-carousel__viewport">
      <button
        class="accessible-carousel__nav accessible-carousel__nav--prev"
        aria-label="Previous slide"
        :disabled="!canGoPrev"
        @click="prev"
      >
        <i class="pi pi-chevron-left" />
      </button>

      <div
        class="accessible-carousel__track"
        aria-live="polite"
        @touchstart="handleTouchStart"
        @touchend="handleTouchEnd"
      >
        <div class="accessible-carousel__slide-container" :style="trackStyle">
          <div v-for="(_, index) in totalSlots" :key="index" class="accessible-carousel__slide" :style="slideStyle">
            <slot :name="`slide-${index}`" :index="index" />
          </div>
        </div>
      </div>

      <button
        class="accessible-carousel__nav accessible-carousel__nav--next"
        aria-label="Next slide"
        :disabled="!canGoNext"
        @click="next"
      >
        <i class="pi pi-chevron-right" />
      </button>
    </div>

    <div class="accessible-carousel__dots" role="tablist" aria-label="Carousel navigation">
      <button
        v-for="dot in totalDots"
        :key="dot"
        role="tab"
        class="accessible-carousel__dot"
        :class="{ 'accessible-carousel__dot--active': dot - 1 === currentIndex }"
        :aria-selected="dot - 1 === currentIndex"
        :aria-label="`Go to slide group ${dot}`"
        @click="goTo(dot - 1)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useBreakpoint } from '@/composables/useBreakpoint';

interface VisibleItems {
  sm: number;
  md: number;
  lg: number;
}

const props = withDefaults(
  defineProps<{
    autoScroll?: boolean;
    interval?: number;
    visibleItems?: VisibleItems;
    totalSlots: number;
    ariaLabel?: string;
  }>(),
  {
    autoScroll: true,
    interval: 7000,
    visibleItems: () => ({ sm: 1, md: 2, lg: 4 }),
    ariaLabel: 'Carousel',
  }
);

const { isBelow } = useBreakpoint();

const currentIndex = ref(0);
const isPaused = ref(false);
const touchStartX = ref(0);
let autoScrollTimer: ReturnType<typeof setInterval> | undefined;

const prefersReducedMotion = ref(false);
let mqlRef: MediaQueryList | undefined;

/**
 * Handles changes to the prefers-reduced-motion media query
 * @param e the media query list event
 */
function handleMotionChange(e: MediaQueryListEvent): void {
  prefersReducedMotion.value = e.matches;
}

onMounted(() => {
  const mql = globalThis.matchMedia?.('(prefers-reduced-motion: reduce)');
  if (mql) {
    mqlRef = mql;
    prefersReducedMotion.value = mql.matches;
    mql.addEventListener('change', handleMotionChange);
  }
  startAutoScroll();
});

onUnmounted(() => {
  stopAutoScroll();
  if (mqlRef) {
    mqlRef.removeEventListener('change', handleMotionChange);
  }
});

const currentVisibleCount = computed((): number => {
  if (isBelow('md')) return props.visibleItems.sm;
  if (isBelow('lg')) return props.visibleItems.md;
  return props.visibleItems.lg;
});

const maxIndex = computed((): number => {
  return Math.max(0, props.totalSlots - currentVisibleCount.value);
});

const totalDots = computed((): number => {
  return maxIndex.value + 1;
});

const canGoPrev = computed((): boolean => currentIndex.value > 0);
const canGoNext = computed((): boolean => currentIndex.value < maxIndex.value);

const slideStyle = computed(() => ({
  flex: `0 0 ${100 / currentVisibleCount.value}%`,
  maxWidth: `${100 / currentVisibleCount.value}%`,
}));

const trackStyle = computed(() => ({
  transform: `translateX(-${(currentIndex.value * 100) / currentVisibleCount.value}%)`,
  transition: prefersReducedMotion.value ? 'none' : 'transform 0.4s ease',
  display: 'flex',
  width: `${(props.totalSlots * 100) / currentVisibleCount.value}%`,
}));

/**
 * Advances the carousel to the next slide, wrapping to start if auto-scroll is enabled
 */
function next(): void {
  if (canGoNext.value) {
    currentIndex.value++;
  } else if (props.autoScroll) {
    currentIndex.value = 0;
  }
}

/**
 * Moves the carousel to the previous slide
 */
function prev(): void {
  if (canGoPrev.value) {
    currentIndex.value--;
  }
}

/**
 * Jumps the carousel to a specific slide index
 * @param index the slide index to navigate to
 */
function goTo(index: number): void {
  currentIndex.value = Math.min(index, maxIndex.value);
}

/**
 * Toggles the auto-scroll pause state
 */
function togglePause(): void {
  isPaused.value = !isPaused.value;
  if (isPaused.value) {
    stopAutoScroll();
  } else {
    startAutoScroll();
  }
}

/**
 * Starts the auto-scroll timer if conditions are met
 */
function startAutoScroll(): void {
  if (!props.autoScroll || prefersReducedMotion.value || isBelow('md')) return;
  stopAutoScroll();
  autoScrollTimer = setInterval(() => {
    if (!isPaused.value) {
      next();
    }
  }, props.interval);
}

/**
 * Stops the auto-scroll timer
 */
function stopAutoScroll(): void {
  if (autoScrollTimer !== undefined) {
    clearInterval(autoScrollTimer);
    autoScrollTimer = undefined;
  }
}

/**
 * Handles keyboard navigation within the carousel
 * @param event the keyboard event
 */
function handleKeydown(event: KeyboardEvent): void {
  if (event.key === 'ArrowLeft') {
    event.preventDefault();
    prev();
  } else if (event.key === 'ArrowRight') {
    event.preventDefault();
    next();
  }
}

/**
 * Records the starting X position of a touch gesture
 * @param event the touch event
 */
function handleTouchStart(event: TouchEvent): void {
  touchStartX.value = event.touches[0].clientX;
}

/**
 * Handles the end of a touch gesture to determine swipe direction
 * @param event the touch event
 */
function handleTouchEnd(event: TouchEvent): void {
  const touchEndX = event.changedTouches[0].clientX;
  const diff = touchStartX.value - touchEndX;
  const threshold = 50;
  if (Math.abs(diff) > threshold) {
    if (diff > 0) {
      next();
    } else {
      prev();
    }
  }
}

watch(
  () => prefersReducedMotion.value,
  (reduced) => {
    if (reduced) {
      stopAutoScroll();
      isPaused.value = true;
    }
  }
);

watch(currentVisibleCount, () => {
  if (currentIndex.value > maxIndex.value) {
    currentIndex.value = maxIndex.value;
  }
  stopAutoScroll();
  startAutoScroll();
});
</script>

<style scoped lang="scss">
.accessible-carousel {
  position: relative;

  &__controls {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 8px;
  }

  &__pause-btn {
    background: none;
    border: 1px solid var(--p-surface-200, #e6e6e6);
    border-radius: 50%;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: var(--p-text-color, #1b1b1b);

    &:hover {
      background: var(--p-surface-100, #f0f0f0);
    }

    &:focus-visible {
      outline: 2px solid var(--p-primary-color);
      outline-offset: 2px;
    }
  }

  &__viewport {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__track {
    overflow: hidden;
    flex: 1;
  }

  &__slide-container {
    display: flex;
  }

  &__slide {
    padding: 0 8px;
    box-sizing: border-box;
  }

  &__nav {
    background: none;
    border: 1px solid var(--p-surface-200, #e6e6e6);
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: var(--p-text-color, #1b1b1b);
    flex-shrink: 0;

    &:hover:not(:disabled) {
      background: var(--p-surface-100, #f0f0f0);
    }

    &:disabled {
      opacity: 0.3;
      cursor: default;
    }

    &:focus-visible {
      outline: 2px solid var(--p-primary-color);
      outline-offset: 2px;
    }
  }

  &__dots {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 16px;
  }

  &__dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: 1px solid var(--p-surface-300, #c0c0c0);
    background: transparent;
    cursor: pointer;
    padding: 0;

    &--active {
      background: var(--p-primary-color);
      border-color: var(--p-primary-color);
    }

    &:focus-visible {
      outline: 2px solid var(--p-primary-color);
      outline-offset: 2px;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .accessible-carousel {
    &__nav {
      display: none;
    }

    &__controls {
      display: none;
    }
  }
}
</style>
