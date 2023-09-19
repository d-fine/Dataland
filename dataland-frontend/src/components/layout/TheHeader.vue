<template>
  <header class="header" role="banner">
    <div class="header__logo">
      <router-link to="/lp" aria-label="Go to the Landing Page" aria-current="page">
        <img
          v-if="landingPage?.sections[0]?.image?.[0]"
          :src="landingPage.sections[0].image[0]"
          alt="Dataland banner logo"
        />
      </router-link>
    </div>
    <TheNavi :contentData="contentData" />
    <AuthSection />
    <!-- <LandingLoginMobile v-if="isMobile" /> -->
  </header>
</template>

<script setup lang="ts">
import AuthSection from "@/components/resources/newLandingPage/AuthSection.vue";
import TheNavi from "@/components/general/TheNavi.vue";
// import LandingLoginMobile from "@/components/resources/landing/LandingLoginMobile.vue";

import type { Content, Page } from "@/types/ContentTypes";

const { landingPage, contentData } = defineProps<{
  landingPage?: Page;
  contentData: Content;
}>();
</script>

<style lang="scss" scoped>
/* Block: Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 46px;
  margin: 16px;

  &__logo {
    img {
      width: 143px;
      height: auto;
    }
  }
}
</style>
