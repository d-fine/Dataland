<template>
  <LandingPageHeader :landingPage="landingPage" />
  <main role="main">
    <TheIntro :sections="landingPage?.sections" />
    <ThePortfolio :sections="landingPage?.sections" />
    <TheQuotes :sections="landingPage?.sections" />
    <TheBrands :sections="landingPage?.sections" />
    <TheStruggle :sections="landingPage?.sections" />
    <TheHowItWorks :sections="landingPage?.sections" />
    <TheJoinCampaign :sections="landingPage?.sections" />
    <TheGetInTouch :sections="landingPage?.sections" />
  </main>
  <TheFooter :isLightVersion="false" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDialog } from 'primevue/usedialog';
import SessionDialog from '@/components/general/SessionDialog.vue';
import { SessionDialogMode } from '@/utils/SessionTimeoutUtils';

import LandingPageHeader from '@/components/generics/LandingPageHeader.vue';
import TheIntro from '@/components/resources/landingPage/TheIntro.vue';
import TheQuotes from '@/components/resources/landingPage/TheQuotes.vue';
import TheBrands from '@/components/resources/landingPage/TheBrands.vue';
import TheStruggle from '@/components/resources/landingPage/TheStruggle.vue';
import TheHowItWorks from '@/components/resources/landingPage/TheHowItWorks.vue';
import TheJoinCampaign from '@/components/resources/landingPage/TheJoinCampaign.vue';
import TheGetInTouch from '@/components/resources/landingPage/TheGetInTouch.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import ThePortfolio from '@/components/resources/landingPage/ThePortfolio.vue';

const content: Content = contentData;
const landingPage: Page | undefined = content.pages.find((page) => page.url === '/');

const dialog = useDialog();
const route = useRoute();
const router = useRouter();

onMounted(() => {
  if (route.query.externalLogout === 'true') {
    openLogoutModal();
  }
});

const openLogoutModal = (): void => {
  dialog.open(SessionDialog, {
    props: {
      modal: true,
      dismissableMask: true,
      showHeader: false,
    },
    data: {
      sessionDialogMode: SessionDialogMode.ExternalLogout,
    },
    onClose: () => {
      void router.replace('');
    },
  });
};
</script>

<style scoped>
main {
  margin-top: 132px;
  @media only screen and (max-width: 768px) {
    margin-top: 52px;
  }
}
</style>
