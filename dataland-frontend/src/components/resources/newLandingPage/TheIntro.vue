<template>
  <section
    v-if="introSection"
    class="intro"
    role="region"
    aria-label="Introduction"
    :style="isMobile && inputFocused ? { marginTop: '0' } : {}"
  >
    <img
      v-for="(img, index) in introSection.image"
      :key="index"
      :src="img"
      :alt="introSection.text.join(' ')"
      class="intro__img"
      v-show="!inputFocused || !isMobile"
    />

    <h1 class="intro__text" v-show="!inputFocused || !isMobile">
      <template v-for="(part, index) in introSection.text" :key="index">
        <span v-if="index === 0 || index === 2">{{ part }}</span>
        <template v-else>{{ part }}</template>
      </template>
    </h1>
    <div class="intro__blurred-overlay" v-if="inputFocused && isMobile"></div>
    <div v-if="inputFocused && isMobile" class="back-button" @click="handleInputBlur">Back</div>

    <CompaniesOnlySearchBar
      @select-company="router.push(`/companies/${$event.companyId}`)"
      wrapper-class="p-input-icon-left p-input-icon-right p-input-icon-align search"
      input-class="h-3rem search__field"
      icon-class="pi pi-search search__icon"
      @focus="handleInputFocus"
      @blur="handleInputBlur"
    />

    <ButtonComponent
      :label="aboutIntroSection?.text[2] || 'EXPLORE OUR PRINCIPLES'"
      buttonType="button-component about__button"
      aria-label="About Page"
      @click="router.push('/about')"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from 'vue';
import type { Section } from '@/types/ContentTypes';
import CompaniesOnlySearchBar from '@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue';
import ButtonComponent from '@/components/resources/newLandingPage/ButtonComponent.vue';
import router from '@/router';

const props = defineProps<{ sections?: Section[] }>();

const introSection = computed(() => {
  return props.sections?.find((section) => section.title === 'Intro') ?? null;
});

const aboutIntroSection = computed(() => {
  return props.sections?.find((section) => section.title === 'START YOUR DATALAND JOURNEY') ?? null;
});

const isMobile = ref(window.innerWidth < 768);

watch(
  () => window.innerWidth,
  (newWidth) => {
    isMobile.value = newWidth < 768;
  }
);

const updateIsMobile = (): void => {
  isMobile.value = window.innerWidth < 768;
};

window.addEventListener('resize', updateIsMobile);

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile);
});

const inputFocused = ref(false);

const handleInputFocus = (): void => {
  inputFocused.value = true;
  if (isMobile.value) {
    const header = document.querySelector('.header') as HTMLElement;
    if (header) header.style.display = 'none';
  }
};

const handleInputBlur = (): void => {
  setTimeout(() => {
    inputFocused.value = false;
    if (isMobile.value) {
      const header = document.querySelector('.header') as HTMLElement;
      if (header) header.style.display = '';
    }
  }, 300);
};
</script>

<style scoped lang="scss">
.intro {
  text-align: center;
  margin: 140px auto;
  max-width: 1007px;
  position: relative;
  z-index: 1;
  &__img {
    width: 85px;
    height: auto;
    margin-bottom: 42px;
  }
  &__text {
    color: var(--basic-dark);
    text-align: center;
    font-size: 100px;
    font-style: normal;
    font-weight: 700;
    line-height: 106px;
    letter-spacing: 0.25px;
    margin: 0;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    span:last-of-type {
      display: block;
      font-size: 48px;
      line-height: 56px;
      margin-top: 80px;
    }
  }
  .button-component.about__button {
    display: none;
  }
}
@media only screen and (max-width: $large) {
  .intro {
    &__img {
      margin-bottom: 31px;
    }
    &__text {
      font-size: 64px;
      line-height: 78px;
      max-width: 750px;
      margin: 0 auto;
      span:last-of-type {
        font-size: 40px;
        font-weight: 600;
        line-height: 48px;
        letter-spacing: 0.25px;
        margin-top: 32px;
      }
    }
  }
}

@media only screen and (max-width: $medium) {
  .intro {
    &__text {
      font-size: 48px;
      line-height: 56px;
      max-width: 534px;
      letter-spacing: unset;
      margin: 0 auto;
      font-weight: 600;
      span:last-of-type {
        font-size: 32px;
        line-height: 40px;
      }
    }
  }
}

@media only screen and (max-width: $small) {
  .intro {
    margin: 64px auto;
    padding-inline: 16px;
    &__img {
      width: 54px;
      margin-bottom: 48px;
    }
    &__text {
      font-size: 40px;
      line-height: 48px;
      max-width: 328px;
      span:last-of-type {
        font-size: 24px;
        line-height: 32px;
      }
    }
    .back-button {
      position: absolute;
      top: -44px;
      left: 32px;
      cursor: pointer;
      z-index: 10;
      text-align: left;
      padding: 0 16px;
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 24px;
      letter-spacing: 0.25px;
      &::before {
        content: '';
        display: block;
        position: absolute;
        left: -8px;
        top: 50%;
        transform: translateY(-50%);
        width: 16px;
        height: 16px;
        background-image: url(/static/icons/Arrow--right.svg);
        transform: rotateY(180deg) translateY(-50%);
      }
    }
    .button-component.about__button {
      display: block;
      background-color: transparent;
      margin: 3em auto;
      color: var(--basic-dark);
      border-color: var(--basic-dark);
      &:hover {
        border-color: var(--primary-orange);
        color: var(--primary-orange);
      }
    }
    & .search {
      z-index: 20;
    }
    &__blurred-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: var(--default-neutral-white);
      z-index: 10;
    }
  }
}
</style>

<style lang="scss">
.search {
  position: relative;
  margin: 80px auto 0;
  height: 80px;
  padding: 28px 24px;
  border-radius: 16px;
  border: 2px solid var(--grey-tones-100);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 4px 32px 0 rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(16px);
  display: flex;
  flex-direction: row;
  align-items: center;

  & .p-inputwrapper {
    width: 100%;
  }

  &__field {
    width: 100%;
    border: none;
    outline: none;
    padding-left: 40px;
    background: transparent;
    font-weight: 600;
    &::placeholder {
      font-size: 16px;
      font-style: normal;

      line-height: 24px;
      letter-spacing: 0.25px;
      color: var(--grey-tones-600);
    }
    &:focus {
      box-shadow: none !important;
      &::placeholder {
        color: var(--grey-tones-300);
      }
    }
  }

  &__autocomplete {
    transform-origin: top !important;
    margin: 27px 0;
    padding: 20px;
    top: 47px !important;
    left: -26px !important;
    width: 1007px !important;
    overflow: hidden;
    border-radius: 16px;
    border: 2px solid var(--grey-tones-100);
    background: var(--default-neutral-white);
    box-shadow: 0 4px 32px 0 rgba(0, 0, 0, 0.08);
    backdrop-filter: blur(16px);
    @media only screen and (max-width: $large) {
      max-width: 701px !important;
      width: calc(100% + 52px) !important;
    }
    ul.p-autocomplete-items {
      max-height: calc(400px - 27px);
      overflow-y: auto;
      border-radius: 8px;
      border: 2px solid var(--grey-tones-100);
      background: var(--default-neutral-white);
      li.p-autocomplete-item {
        height: 4rem;
        position: relative;
        &:after {
          content: '';
          height: 2px;
          width: calc(100% - 3em);
          background-color: var(--grey-tones-100);
          position: absolute;
          bottom: 0;
          left: 1.5em;
        }
        &:last-child:after {
          content: none;
        }
        &:hover {
          background-color: var(--grey-tones-100) !important;
        }
        i.pi.pi-search.pl-3 {
          padding-left: 2em !important;
        }
      }
    }
    ul.p-autocomplete-items.pt-0 {
      display: none;
    }
  }

  &__icon {
    font-size: 18px;
    pointer-events: none;
  }
}
@media only screen and (max-width: $large) {
  .search {
    margin-top: 31px;
    max-width: 701px;
  }
}
@media only screen and (max-width: $small) {
  .search__autocomplete {
    padding: 0px;
    top: 47px !important;
    left: -26px !important;
    overflow: hidden;
    border-radius: 0;
    border: none;
    background: none;
    box-shadow: none;
    backdrop-filter: unset;
    max-height: 100vw !important;
    ul.p-autocomplete-items {
      border-radius: 0;
      background: none;
      border: 0;
      li.p-autocomplete-item i.pi.pi-search.pl-3 {
        padding-left: 1.7em !important;
      }
      li.p-autocomplete-item:after {
        width: calc(100% - 3em);
        left: 1.5em;
      }
    }
  }
}
</style>
