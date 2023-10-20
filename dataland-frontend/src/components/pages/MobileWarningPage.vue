<template>
  <TheHeader :landingPage="landingPage" />
  <main role="main">
    <h1>hellohelloHIhello</h1>
  </main>
  <TheFooter :sections="landingPage?.sections" />
</template>

<script setup lang="ts">
import TheHeader from "@/components/layout/TheHeader.vue";
import TheFooter from "@/components/layout/TheFooter.vue";
import contentData from "@/assets/content.json";
import type { Content, Page } from "@/types/ContentTypes";

const content: Content = contentData;
const landingPage: Page | undefined = content.pages.find((page) => page.url === "/");
</script>

<style lang="scss" scoped>
main {
  margin-top: 132px;
  @media only screen and (max-width: $small) {
    margin-top: 52px;
  }
}
</style>
