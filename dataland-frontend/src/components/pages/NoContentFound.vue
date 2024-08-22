<template>
  <TheHeader :showUserProfileDropdown="false" />
  <TheContent>
    <h2>Sorry an error occurred!</h2>
    <p>Whoops! Something went wrong!</p>
    <p>
      Go back to
      <router-link to="/" class="text-primary" title="back to landing page">Dataland</router-link>
    </p>
  </TheContent>
  <TheFooter :is-light-version="true" :sections="footerContent" />
</template>

<script lang="ts">
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page, Section } from '@/types/ContentTypes';

export default {
  name: 'NoContentFound',
  components: { TheHeader, TheContent, TheFooter },

  data(): { footerContent: Section[] | undefined } {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      footerContent,
    };
  },
};
</script>
