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

    <AuthSection />
  </header>
</template>

<script setup lang="ts">
import AuthSection from "@/components/resources/newLandingPage/AuthSection.vue";

import type { Page } from "@/types/ContentTypes";

const { landingPage } = defineProps<{
  landingPage?: Page;
}>();
</script>

<style lang="scss" scoped>
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
  z-index: 1;
  background: rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  box-sizing: border-box;

  &__logo {
    img {
      width: 143px;
      height: auto;
    }
  }
}
</style>
