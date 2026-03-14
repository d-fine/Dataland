<template>
  <header class="header" :class="{ 'header--menu-open': menuOpen }" role="banner">
    <a href="#main-content" class="skip-link">Skip to content</a>
    <div class="header__logo">
      <router-link to="/" aria-label="Go to the Landing Page" aria-current="page">
        <img src="/static/logos/gfx_logo_dataland_orange_S.svg" alt="Dataland banner logo" />
      </router-link>
    </div>
    <nav id="mobile-menu" class="header__navigation" :class="{ 'header__navigation--open': menuOpen }">
      <Button
        to="/"
        class="header__link"
        :class="{ 'active-link': isActiveHome }"
        label="HOME"
        variant="text"
        @click="handleNavClick('/')"
      />
      <Button
        to="/"
        class="header__link"
        :class="{ 'active-link': isActiveAbout }"
        label="ABOUT"
        variant="text"
        @click="handleNavClick('/about')"
      />
    </nav>
    <div class="header__auth" :class="{ 'header__auth--visible': menuOpen }">
      <AuthSection :is-landing-page="true" />
    </div>
    <button
      class="header__hamburger"
      :aria-expanded="menuOpen"
      aria-controls="mobile-menu"
      aria-label="Toggle navigation menu"
      @click="menuOpen = !menuOpen"
    >
      <i :class="menuOpen ? 'pi pi-times' : 'pi pi-bars'" />
    </button>
  </header>
</template>

<script setup lang="ts">
import AuthSection from '@/components/resources/landingPage/AuthSection.vue';
import { useRoute } from 'vue-router';
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import router from '@/router';

const route = useRoute();
const menuOpen = ref(false);

const isActiveHome = computed(() => route.path === '/');
const isActiveAbout = computed(() => route.path === '/about');

const handleNavClick = (path: string): void => {
  menuOpen.value = false;
  void router.push(path);
};
</script>

<style lang="scss" scoped>
.skip-link {
  position: absolute;
  left: -9999px;
  z-index: 999;
  padding: 0.5rem 1rem;
  background: var(--p-primary-color);
  color: #fff;
  font-weight: 600;
  text-decoration: none;

  &:focus {
    left: 1rem;
    top: 1rem;
  }
}

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
    color: var(--p-highlight-color);
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
      color: var(--p-primary-color);
      border-bottom: 2px solid var(--p-primary-color);
    }
  }

  &__hamburger {
    display: none;
    align-items: center;
    justify-content: center;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1.25rem;
    color: var(--p-text-color, #1b1b1b);
    padding: 0.5rem;
  }
}

@media only screen and (max-width: $bp-md) {
  .header {
    padding: 1rem;
    margin: 0;
    width: 100%;
    border-radius: 0;
    flex-wrap: wrap;

    .header__logo {
      img {
        width: 79px;
      }
    }

    &.header--menu-open {
      background: rgba(255, 255, 255, 0.96);
    }

    .header__navigation {
      max-height: 0;
      overflow: hidden;
      transition: max-height 0.3s ease;
      width: 100%;
      flex-direction: column;
      gap: 1rem;
      padding: 0;
      display: flex;
    }

    .header__navigation--open {
      max-height: 500px;
      padding: 1rem 0;
    }

    .header__auth {
      max-height: 0;
      overflow: hidden;
      transition: max-height 0.3s ease;
      width: 100%;
      justify-content: center;
      padding: 0;
      display: flex;
    }

    .header__auth--visible {
      max-height: 200px;
      padding: 0.5rem 0;
    }

    .header__hamburger {
      display: flex;
    }
  }
}
</style>
