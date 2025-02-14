<template>
  <TheHeader :landingPage="landingPage" />
  <div class="static-content">
    <main role="main" class="w-12 flex justify-content-center">
      <div class="lg:w-6 md:w-10 sm:w-10 w-12 text-left">
        <slot />
      </div>
    </main>
    <TheFooter :sections="landingPage?.sections" :is-light-version="false" />
  </div>
</template>

<script lang="ts">
import TheFooter from '@/components/generics/TheNewFooter.vue';
import TheHeader from '@/components/generics/TheNewHeader.vue';
import contentData from '@/assets/content.json';
import type { Content } from '@/types/ContentTypes';
import { defineComponent } from 'vue';

const content: Content = contentData;

export default defineComponent({
  name: 'LegalPage',
  components: {
    TheHeader,
    TheFooter,
  },
  data() {
    return {
      landingPage: content.pages.find((page) => page.url === '/'),
    };
  },
});
</script>

<style lang="scss" scoped>
@use '@/assets/scss/newVariables';

.static-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100vh;
}

main {
  margin-top: 122px;
  margin-bottom: 52px;
  @media only screen and (max-width: newVariables.$small) {
    margin-top: 82px;
  }
}
</style>
