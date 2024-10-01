<template>
  <SlideShow
    slides-container-classes="test__slides"
    arrows-container-classes="test__arrows"
    left-arrow-classes="test__arrow test__arrow--left"
    right-arrow-classes="test__arrow test__arrow--right"
    :slide-count="3"
    :scroll-screen-width-limit="1800"
  >
    <ul>
      <li v-for="index in [0, 1, 2]" :key="index" class="test__slide">
        <p>Content</p>
      </li>
    </ul>
  </SlideShow>
</template>

<script setup lang="ts">
// @ts-nocheck
import SlideShow from '@/components/general/SlideShow.vue';
</script>

<style lang="scss">
.test {
  padding: 200px 0;
  background-color: var(--primary-orange);

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
  }

  &__slides {
    display: flex;
    transition: transform 0.3s ease-out;
    gap: 32px;
    justify-content: center;

    &.isdragging .test__slide {
      cursor: grabbing;
    }
  }

  &__slide {
    flex: 0 0 408px;
    border-radius: 16px;
    display: flex;
    padding: 64px 40px 32px 40px;
    flex-direction: column;
    background: #f6f6f6;
    gap: 24px;
    cursor: grab;
    text-align: left;

    &-title {
      font-size: 48px;
      font-style: normal;
      font-weight: 600;
      line-height: 56px; /* 116.667% */
      letter-spacing: 0.25px;
      margin: 0;
    }

    &-text {
      font-size: 20px;
      font-style: normal;
      font-weight: 400;
      line-height: 28px; /* 140% */
      letter-spacing: 0.25px;
      color: #585858;
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
    gap: 18px;
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
      }
    }
  }
}
</style>
