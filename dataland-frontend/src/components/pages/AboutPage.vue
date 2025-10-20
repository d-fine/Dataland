<template>
  <main role="main">
    <TheAboutIntro :sections="aboutPage?.sections" />
    <TheAboutPrinciples :sections="aboutPage?.sections" />
  </main>
</template>

<script setup lang="ts">
import TheAboutIntro from '@/components/resources/aboutPage/TheAboutIntro.vue';
import TheAboutPrinciples from '@/components/resources/aboutPage/TheAboutPrinciples.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';

const content: Content = contentData;
const aboutPage: Page | undefined = content.pages.find((page) => page.url === '/about');
</script>

<style lang="scss" scoped>
main {
  margin-top: 132px;
  @media only screen and (max-width: 768px) {
    margin-top: 80px;
  }
}
</style>
