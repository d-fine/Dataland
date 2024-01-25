<template>
  <section v-if="introSection" class="intro" role="region" aria-label="Introduction">
    <img
      v-for="(img, index) in introSection.image"
      :key="index"
      :src="img"
      :alt="introSection.text.join(' ')"
      class="intro__img"
    />

    <h1 class="intro__text">
      <template v-for="(part, index) in introSection.text" :key="index">
        <span v-if="index === 0 || index === 2">{{ part }}</span>
        <template v-else>{{ part }}</template>
      </template>
    </h1>
    <CompaniesOnlySearchBar
      @select-company="$router.push(`/companies/${$event.companyId}`)"
      wrapper-class="p-input-icon-left p-input-icon-right p-input-icon-align search"
      input-class="h-3rem search__field"
      icon-class="pi pi-search search__icon"
    />
    <ButtonComponent
      :label="aboutIntroSection?.text[2] || 'EXPLORE OUR PRINCIPLES'"
      buttonType="button-component about__button"
      aria-label="About Page"
      @click="$router.push('/about')"
    />
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";
import CompaniesOnlySearchBar from "@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue";
import ButtonComponent from "@/components/resources/newLandingPage/ButtonComponent.vue";

const props = defineProps<{ sections?: Section[] }>();

const introSection = computed(() => {
  return props.sections?.find((section) => section.title === "Intro") ?? null;
});

const aboutIntroSection = computed(() => {
  return props.sections?.find((section) => section.title === "START YOUR DATALAND JOURNEY") ?? null;
});
</script>

<style scoped lang="scss">
.intro {
  text-align: center;
  margin: 140px auto;
  max-width: 1007px;
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
      line-height: 56px; /* 116.667% */
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
        line-height: 48px; /* 120% */
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
      span:last-of-type {
        font-size: 32px;
        line-height: 40px; /* 125% */
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
    &::placeholder {
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 24px; /* 150% */
      letter-spacing: 0.25px;
      color: var(--grey-tones-400);
    }
  }

  &__icon {
    font-size: 18px;
    pointer-events: none; // Let click events pass through to the input
  }
}
@media only screen and (max-width: $large) {
  .search {
    margin-top: 31px;
    max-width: 701px;
  }
}
</style>
