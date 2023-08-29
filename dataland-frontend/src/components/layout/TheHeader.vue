<template>
  <router-link to="/lp" aria-label="Go to the Landing Page">
    <img :src="landingPage?.sections[0]?.image" alt="Dataland banner logo" />
  </router-link>

  <TheNavi :contentData="contentData" />
  <LandingLogin v-if="!isMobile" />
  <!-- <LandingLoginMobile v-if="isMobile" /> -->
  <!-- Show title and URL if they exist -->
</template>

<script setup lang="ts">
import LandingLogin from "@/components/resources/newLandingPage/LandingLogin.vue";
import TheNavi from "@/components/general/TheNavi.vue";
// import LandingLoginMobile from "@/components/resources/landing/LandingLoginMobile.vue";

import type { Content, Page } from "@/types/ContentTypes";

const { isMobile, landingPage, contentData } = defineProps<{
  isMobile: boolean;
  landingPage?: Page;
  contentData: Content;
}>();
</script>

<style scoped></style>
