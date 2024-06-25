<template>
  <section
    v-if="aboutPrinciplesSection"
    class="about-principles"
    role="region"
    aria-labelledby="about-principles-heading"
    data-test="about-principles"
  >
    <div class="about-principles__wrapper">
      <h2 id="about-principles-heading" class="about-principles__title">
        {{ sectionTitle }}
      </h2>
      <SlideShow
        slides-wrapper-classes="about-principles__sliderwrapper"
        slides-container-classes="about-principles__slides"
        arrows-container-classes="about-principles__arrows"
        left-arrow-classes="about-principles__arrow about-principles__arrow--left"
        right-arrow-classes="about-principles__arrow about-principles__arrow--right"
        :slide-count="slides.length"
        :scroll-screen-width-limit="9999"
        :slide-width="slideWidth"
      >
        <div v-for="(slide, index) in slides" :key="index" role="listitem" class="about-principles__slide">
          <h3 class="about-principles__slide-title">{{ slide.title }}</h3>
          <p class="about-principles__slide-text">{{ slide.text }}</p>
          <p class="about-principles__slide-index">0{{ index + 1 }}</p>
        </div>
      </SlideShow>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import type { Section } from '@/types/ContentTypes';
import SlideShow from '@/components/general/SlideShow.vue';

const { sections } = defineProps<{ sections?: Section[] }>();
const aboutPrinciplesSection = computed(() => sections?.find((s) => s.title === 'Our principles'));
const sectionTitle = computed(() => aboutPrinciplesSection.value?.text.join(' ') ?? '');
const slides = computed(() => aboutPrinciplesSection.value?.cards ?? []);

const slideWidth = ref(440);

const updateSlideWidth = (): void => {
  slideWidth.value = window.innerWidth > 768 ? 440 : 320;
};
updateSlideWidth();

onMounted(() => {
  window.addEventListener('resize', updateSlideWidth);
  updateSlideWidth();
});

onUnmounted(() => {
  window.removeEventListener('resize', updateSlideWidth);
});
</script>

<style lang="scss">
.about-principles {
  padding: 200px 0;
  background-color: var(--grey-tones-100);

  &__wrapper {
    position: relative;
    overflow: hidden;
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    padding-left: calc((100% - 900px) / 2 + 22px);
    align-items: center;
    gap: 56px;
  }

  &__title {
    grid-column: 1 / 8;
    font-size: 48px;
    font-style: normal;
    font-weight: 600;
    line-height: 56px;
    letter-spacing: 0.25px;
    margin: 0;
    max-width: 370px;
    text-align: left;
    display: block;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    -webkit-transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    -moz-transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    -o-transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
  }
  &__sliderwrapper,
  &__arrows {
    grid-column: 1 / -1;
  }

  &__slides {
    display: flex;
    display: -webkit-flex;
    display: -ms-flexbox;
    transition: transform 0.3s ease-out;
    -webkit-transition: transform 0.3s ease-out;
    -moz-transition: transform 0.3s ease-out;
    -o-transition: transform 0.3s ease-out;
    gap: 32px;
    justify-content: flex-start;

    &.isdragging .about-principles__slide {
      cursor: grabbing;
    }
  }

  &__slide {
    border-radius: 16px;
    -webkit-border-radius: 16px;
    -moz-border-radius: 16px;
    display: flex;
    display: -webkit-flex;
    display: -ms-flexbox;
    flex: 0 0 408px;
    -webkit-box-flex: 0;
    padding: 64px 40px 32px 40px;
    flex-direction: column;
    background: var(--default-neutral-white);
    gap: 24px;
    text-align: left;
    cursor: grab;

    &-title {
      font-size: 40px;
      line-height: 48px;
      font-style: normal;
      font-weight: 600;
      letter-spacing: 0.25px;
      margin: 0;
      transition:
        font-size 0.4s ease,
        line-height 0.4s ease;
      -webkit-transition:
        font-size 0.4s ease,
        line-height 0.4s ease;
      -moz-transition:
        font-size 0.4s ease,
        line-height 0.4s ease;
      -o-transition:
        font-size 0.4s ease,
        line-height 0.4s ease;
    }

    &-text {
      font-size: 20px;
      font-style: normal;
      font-weight: 400;
      line-height: 28px; /* 140% */
      letter-spacing: 0.25px;
      color: #585858;
      margin: 0;
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
    display: -webkit-flex;
    display: -ms-flexbox;
    gap: 18px;
    visibility: visible;
    justify-content: flex-start;
    touch-action: manipulation;
    -ms-touch-action: manipulation;
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
        -webkit-transform: scaleX(-1);
        -moz-transform: scaleX(-1);
        -o-transform: scaleX(-1);
      }
    }
  }
}

@media only screen and (max-width: $medium) {
  .about-principles {
    &__title {
      font-size: 40px;
      line-height: 48px;
    }
  }
}
@media only screen and (max-width: $small) {
  .about-principles {
    padding: 0 0 80px;
    &__wrapper {
      gap: 40px 16px;
      padding: 0 16px;
    }
    &__title {
      font-size: 32px;
      line-height: 40px;
      display: none;
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
  }
}
</style>
