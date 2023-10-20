<template>
  <main role="main">
    <LandingLoginMobile />
  </main>
  <TheFooter :sections="landingPage?.sections" />
</template>

<script setup lang="ts">
import TheFooter from "@/components/layout/TheFooter.vue";
import contentData from "@/assets/content.json";
import LandingLoginMobile from "@/components/resources/newLandingPage/LandingLoginMobile.vue";
import type { Content, Page } from "@/types/ContentTypes";

const content: Content = contentData;
const landingPage: Page | undefined = content.pages.find((page) => page.url === "/");
</script>

<style scoped lang="scss">
.d-text-register {
  color: #5a4f36;
}
.mobile-logo {
  text-align: center;
  margin: 2rem;
  width: 100%;
}
.center-divs-row-wrapper {
  justify-content: center;
  .center-divs-row {
    display: flex;
    text-align: left;
    width: 80%;
    justify-content: space-around;
    max-width: 400px;
    margin: 1rem;
  }
}
.imgs-in-line {
  display: flex;
  justify-content: space-between;
  width: 100%;
  height: 60px;
  align-items: flex-end;
  img {
    margin: 0.5rem 2vw 0.5rem 0;
  }
}
</style>
