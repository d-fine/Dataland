<template>
  <section
    v-if="howItWorksSection"
    class="dataaccess"
    role="region"
    aria-labelledby="dataaccess-heading"
    data-test="dataaccess"
  >
    <div class="dataaccess__wrapper">
      <h2 id="dataaccess-heading" class="dataaccess__title">
        {{ sectionText }}
      </h2>
      <SlideShow
        slides-wrapper-classes="dataaccess__sliderwrapper"
        slides-container-classes="dataaccess__slides"
        arrows-container-classes="dataaccess__arrows"
        left-arrow-classes="dataaccess__arrow dataaccess__arrow--left"
        right-arrow-classes="dataaccess__arrow dataaccess__arrow--right"
        :slide-count="slides.length"
        :scroll-screen-width-limit="1800"
        :slide-width="slideWidth"
      >
        <ul class="dataaccess__list">
          <li v-for="(slide, index) in slides" :key="index" class="dataaccess__slide">
            <h3 class="dataaccess__slide-title">{{ slide.title }}</h3>
            <p class="dataaccess__slide-text">{{ slide.text }}</p>
            <p class="dataaccess__slide-index">0{{ index + 1 }}</p>
          </li>
        </ul>
      </SlideShow>

      <!-- [PLACEHOLDER] No video file exists yet. Shows poster SVG as fallback. -->
      <div class="dataaccess__video" ref="videoContainer" data-test="dataaccess-video">
        <video
          ref="videoEl"
          autoplay
          muted
          loop
          playsinline
          preload="none"
          poster="/static/videos/platform-demo-thumb.svg"
          class="dataaccess__video-player"
        >
          <source :src="videoSrc" type="video/mp4" />
        </video>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import type { Section } from '@/types/ContentTypes';
import SlideShow from '@/components/general/SlideShow.vue';
import { useBreakpoint } from '@/composables/useBreakpoint';

const { sections } = defineProps<{ sections?: Section[] }>();

const { isMobile } = useBreakpoint();

const howItWorksSection = computed(() => sections?.find((s) => s.title === 'How it works'));
const sectionText = computed(() => howItWorksSection.value?.text.join(' ') ?? '');
const slides = computed(() => howItWorksSection.value?.cards ?? []);
const slideWidth = computed(() => (isMobile() ? 320 : 440));

// Video lazy-loading via IntersectionObserver
const videoContainer = ref<HTMLElement | null>(null);
const videoSrc = ref('');
const videoEl = ref<HTMLVideoElement | null>(null);
let observer: IntersectionObserver | null = null;

onMounted(() => {
  if (!videoContainer.value) return;
  observer = new IntersectionObserver(
    ([entry]) => {
      if (entry?.isIntersecting) {
        videoSrc.value = '/static/videos/platform-demo.mp4';
        observer?.disconnect();
        observer = null;
      }
    },
    { rootMargin: '200px' }
  );
  observer.observe(videoContainer.value);
});

onUnmounted(() => {
  observer?.disconnect();
  observer = null;
});
</script>

<style lang="scss">
.dataaccess__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  gap: 1rem;
  justify-content: center;
  width: 100%;
}

.dataaccess {
  padding: 200px 0;
  background-color: var(--p-primary-color);

  &__wrapper {
    position: relative;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 56px;
  }

  &__title {
    font-size: 64px;
    font-style: normal;
    font-weight: 700;
    line-height: 78px;
    margin: 0;
    max-width: 1273px;
    padding-right: 789px;
    text-align: left;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
  }

  &__slides {
    display: flex;
    transition: transform 0.3s ease-out;
    gap: 32px;
    justify-content: center;

    &.isdragging .dataaccess__slide {
      cursor: grabbing;
    }
  }

  &__slide {
    border-radius: 16px;
    display: flex;
    flex: 0 0 408px;
    padding: 64px 40px 32px 40px;
    flex-direction: column;
    background: var(--grey-tones-100);
    gap: 24px;
    text-align: left;

    &-title {
      font-size: 48px;
      font-style: normal;
      font-weight: 600;
      line-height: 56px;
      letter-spacing: 0.25px;
      margin: 0;
      transition:
        font-size 0.4s ease,
        line-height 0.4s ease;
    }

    &-text {
      font-size: 20px;
      font-style: normal;
      font-weight: 400;
      line-height: 28px;
      letter-spacing: 0.25px;
      color: #585858;
      margin: 0;
    }

    &-index {
      margin: auto 0 0;
      font-size: 48px;
      font-style: normal;
      font-weight: 600;
      line-height: 56px;
      letter-spacing: 0.25px;
      color: #ff5c00;
    }
  }

  &__arrows {
    display: flex;
    gap: 18px;
    visibility: hidden;
    touch-action: manipulation;
  }

  &__arrow {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid rgba(203, 203, 203, 0.24);
    background-color: var(--default-neutral-white);
    cursor: pointer;

    &:hover {
      border: 2px solid #585858;
    }

    &--left,
    &--right {
      &::before {
        content: '';
        display: block;
        width: 24px;
        height: 24px;
        background-image: url('/static/icons/Arrow--right.svg');
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

  &__video {
    max-width: 800px;
    width: 100%;
    margin: 0 auto;
    aspect-ratio: 16 / 9;
    border-radius: 16px;
    overflow: hidden;
  }

  &__video-player {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    border-radius: 16px;
  }
}

@media only screen and (max-width: 1800px) {
  .dataaccess {
    padding: 120px 0 64px;

    &__wrapper {
      display: grid;
      grid-template-columns: repeat(12, 1fr);
      padding-left: calc((100% - 900px) / 2 + 22px);
    }

    &__title {
      grid-column: 1 / 8;
      font-size: 48px;
      font-weight: 600;
      line-height: 56px;
      letter-spacing: 0.25px;
      max-width: 370px;
      padding-right: unset;
    }

    &__slides {
      justify-content: flex-start;
    }

    &__arrows {
      visibility: visible;
      justify-content: flex-start;
    }

    &__sliderwrapper,
    &__arrows {
      grid-column: 1 / -1;
    }

    &__video {
      grid-column: 1 / -1;
      justify-self: center;
      padding: 0 22px;
    }

    &__slide {
      cursor: grab;

      &-title {
        font-size: 40px;
        line-height: 48px;
      }
    }
  }
}

@media only screen and (max-width: $bp-lg) {
  .dataaccess {
    &__title {
      font-size: 40px;
      line-height: 48px;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .dataaccess {
    padding: 80px 0;

    &__wrapper {
      gap: 40px 16px;
      padding: 0 16px;
    }

    &__title {
      font-size: 32px;
      line-height: 40px;
    }

    &__arrows {
      visibility: visible;
      justify-content: center;
    }

    &__slides {
      gap: 12px;
    }

    &__slide {
      flex: 0 0 308px;
      padding: 48px 32px 24px;
      gap: 16px;

      &-title {
        font-size: 32px;
        line-height: 40px;
      }
    }

    &__video {
      border-radius: 8px;
    }

    &__video-player {
      border-radius: 8px;
    }
  }
}
</style>
