<template>
  <TheNewHeader :landingPage="landingPage" />
  <main role="main">
    <TheIntro :sections="landingPage?.sections" />
    <TheQuotes :sections="landingPage?.sections" />
    <TheBrands :sections="landingPage?.sections" />
    <TheStruggle :sections="landingPage?.sections" />
    <TheHowItWorks :sections="landingPage?.sections" />
    <TheJoinCampaign :sections="landingPage?.sections" />
    <TheGetInTouch :sections="landingPage?.sections" />
  </main>
  <TheFooter :sections="landingPage?.sections" :isLightVersion="false" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDialog } from 'primevue/usedialog';
import SessionDialog from '@/components/general/SessionDialog.vue';
import { SessionDialogMode } from '@/utils/SessionTimeoutUtils';

import TheNewHeader from '@/components/generics/TheNewHeader.vue';
import TheIntro from '@/components/resources/newLandingPage/TheIntro.vue';
import TheQuotes from '@/components/resources/newLandingPage/TheQuotes.vue';
import TheBrands from '@/components/resources/newLandingPage/TheBrands.vue';
import TheStruggle from '@/components/resources/newLandingPage/TheStruggle.vue';
import TheHowItWorks from '@/components/resources/newLandingPage/TheHowItWorks.vue';
import TheJoinCampaign from '@/components/resources/newLandingPage/TheJoinCampaign.vue';
import TheGetInTouch from '@/components/resources/newLandingPage/TheGetInTouch.vue';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';

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

<style lang="scss" scoped>
main {
  margin-top: 132px;
  @media only screen and (max-width: $small) {
    margin-top: 52px;
  }
}
</style>
