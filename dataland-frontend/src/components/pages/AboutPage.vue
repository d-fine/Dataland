<template>
  <TheHeader :landingPage="landingPage" />
  <main role="main">
    <TheAboutIntro :sections="aboutPage?.sections" />
    <TheAboutPrinciples :sections="aboutPage?.sections" />
  </main>
  <TheFooter />
</template>

<script setup lang="ts">
import TheHeader from '@/components/generics/TheNewHeader.vue';
import TheAboutIntro from '@/components/resources/aboutPage/TheAboutIntro.vue';
import TheAboutPrinciples from '@/components/resources/aboutPage/TheAboutPrinciples.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';

const content: Content = contentData;
const landingPage: Page | undefined = content.pages.find((page) => page.url === '/');
const aboutPage: Page | undefined = content.pages.find((page) => page.url === '/about');
</script>

<style lang="scss" scoped>
@use '@/assets/scss/newVariables';

main {
  margin-top: 132px;
  @media only screen and (max-width: newVariables.$small) {
    margin-top: 80px;
  }
}
</style>
