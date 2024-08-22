<template>
  <TheHeader />
  <div class="surface-ground pb-8">
    <h2 class="font-light d-letters text-4xl m-6 text-left">
      We are working on making the website display properly on mobile. <br />
      For now we invite you to use the desktop version.
    </h2>
  </div>
  <TheFooter :sections="landingPage?.sections" />
</template>

<script setup lang="ts">
import TheFooter from '@/components/generics/TheFooter.vue';
import contentData from '@/assets/content.json';
import TheHeader from '@/components/generics/TheHeader.vue';
import type { Content, Page } from '@/types/ContentTypes';

const content: Content = contentData;
const landingPage: Page | undefined = content.pages.find((page) => page.url === '/');
</script>
