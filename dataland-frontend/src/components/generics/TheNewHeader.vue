<template>
  <header class="header" role="banner">
    <div class="header__logo">
      <router-link to="/" aria-label="Go to the Landing Page" aria-current="page">
        <img
          v-if="landingPage?.sections[0]?.image?.[0]"
          :src="landingPage.sections[0].image[0]"
          alt="Dataland banner logo"
        />
      </router-link>
    </div>
    <nav class="header__navigation">
      <router-link to="/" class="header__link" :class="{ 'active-link': isActiveHome }">HOME</router-link>
      <router-link to="/about" class="header__link" :class="{ 'active-link': isActiveAbout }">ABOUT</router-link>
    </nav>
    <AuthSection :is-landing-page="true" />
  </header>
</template>

<script setup lang="ts">
import AuthSection from '@/components/resources/newLandingPage/AuthSection.vue';
import { useRoute } from 'vue-router';
import { computed } from 'vue';
import type { Page } from '@/types/ContentTypes';

const route = useRoute();

const { landingPage } = defineProps<{
  landingPage?: Page;
}>();

const isActiveHome = computed(() => route.path === '/');
const isActiveAbout = computed(() => route.path === '/about');
</script>

<style lang="scss" scoped>
@use '@/assets/scss/newVariables';

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 46px;
  margin: 16px;
  position: fixed;
  width: calc(100% - 32px);
  -webkit-backdrop-filter: blur(16px);
  backdrop-filter: blur(16px);
  z-index: 2;
  background: rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  box-sizing: border-box;

  &__logo {
    img {
      width: 143px;
      height: auto;
    }
  }
  &__navigation {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-grow: 1;
    gap: 2em;
  }

  &__link {
    color: var(--basic-dark);
    font-size: 16px;
    font-style: normal;
    font-weight: 600;
    line-height: 24px;
    letter-spacing: 0.75px;
    text-transform: uppercase;
    text-decoration: none;
    border-bottom: 2px solid transparent;
    &:hover,
    &.active-link {
      color: var(--primary-orange);
      border-bottom: 2px solid var(--primary-orange);
    }
  }
}
@media only screen and (max-width: newVariables.$small) {
  .header {
    padding: 16px;
    margin: 0;
    width: 100%;
    border-radius: 0;
    &__logo {
      img {
        width: 79px;
      }
    }
    &__navigation {
      display: none;
    }
  }
}
</style>
