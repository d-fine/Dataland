<template>
  <section
    v-if="howItWorksSection"
    class="howitworks"
    role="region"
    aria-labelledby="howitworks-heading"
    data-test="howitworks"
  >
    <div class="howitworks__wrapper">
      <h2 id="howitworks-heading" class="howitworks__title">
        {{ sectionText }}
      </h2>
      <SlideShow
        slides-wrapper-classes="howitworks__sliderwrapper"
        slides-container-classes="howitworks__slides"
        arrows-container-classes="howitworks__arrows"
        left-arrow-classes="howitworks__arrow howitworks__arrow--left"
        right-arrow-classes="howitworks__arrow howitworks__arrow--right"
        :slide-count="slides.length"
        :scroll-screen-width-limit="1800"
        :slide-width="slideWidth"
      >
        <div v-for="(slide, index) in slides" :key="index" role="listitem" class="howitworks__slide">
          <h3 class="howitworks__slide-title">{{ slide.title }}</h3>
          <p class="howitworks__slide-text">{{ slide.text }}</p>
          <p class="howitworks__slide-index">0{{ index + 1 }}</p>
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
const howItWorksSection = computed(() => sections?.find((s) => s.title === 'How it works'));
const sectionText = computed(() => howItWorksSection.value?.text.join(' ') ?? '');
const slides = computed(() => sections?.find((s) => s.title === 'How it works')?.cards ?? []);

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
@use '@/assets/scss/newVariables';

.howitworks {
  padding: 200px 0;
  background-color: var(--primary-orange);

  &__wrapper {
    position: relative;
    overflow: hidden;
    display: flex;
    display: -webkit-flex;
    display: -ms-flexbox;
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

  &__slides {
    display: flex;
    display: -webkit-flex;
    display: -ms-flexbox;
    transition: transform 0.3s ease-out;
    -webkit-transition: transform 0.3s ease-out;
    -moz-transition: transform 0.3s ease-out;
    -o-transition: transform 0.3s ease-out;
    gap: 32px;
    justify-content: center;

    &.isdragging .howitworks__slide {
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
    background: #f6f6f6;
    gap: 24px;
    text-align: left;

    &-title {
      font-size: 48px;
      font-style: normal;
      font-weight: 600;
      line-height: 56px; /* 116.667% */
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
    visibility: hidden;
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

@media only screen and (max-width: newVariables.$extra-large) {
  .howitworks {
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
      line-height: 56px; /* 116.667% */
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
    &__slide {
      cursor: grab;
      &-title {
        font-size: 40px;
        line-height: 48px;
      }
    }
  }
}

@media only screen and (max-width: newVariables.$medium) {
  .howitworks {
    &__title {
      font-size: 40px;
      line-height: 48px;
    }
  }
}
@media only screen and (max-width: newVariables.$small) {
  .howitworks {
    padding: 80px 0;
    &__wrapper {
      gap: 40px 16px;
      padding: 0 16px;
    }
    &__title {
      font-size: 32px;
      line-height: 40px;
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
