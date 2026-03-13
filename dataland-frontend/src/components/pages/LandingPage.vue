<template>
  <main id="main-content" role="main">
    <TheIntro :sections="landingPage?.sections" />
    <TheStruggle :sections="landingPage?.sections" />
    <TheTrustBar />
    <TheDataAccess :sections="landingPage?.sections" />
    <TheFrameworks :sections="landingPage?.sections" />
    <TheSocialProof :sections="landingPage?.sections" />
    <TheBrands :sections="landingPage?.sections" />
    <TheGetInTouch :sections="landingPage?.sections" />
  </main>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDialog } from 'primevue/usedialog';
import SessionDialog from '@/components/general/SessionDialog.vue';
import { SessionDialogMode } from '@/utils/SessionTimeoutUtils';

import TheIntro from '@/components/resources/landingPage/TheIntro.vue';
import TheStruggle from '@/components/resources/landingPage/TheStruggle.vue';
import TheTrustBar from '@/components/resources/landingPage/TheTrustBar.vue';
import TheDataAccess from '@/components/resources/landingPage/TheDataAccess.vue';
import TheFrameworks from '@/components/resources/landingPage/TheFrameworks.vue';
import TheSocialProof from '@/components/resources/landingPage/TheSocialProof.vue';
import TheBrands from '@/components/resources/landingPage/TheBrands.vue';
import TheGetInTouch from '@/components/resources/landingPage/TheGetInTouch.vue';
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

<style scoped lang="scss">
main {
  margin-top: 132px;
  @media only screen and (max-width: $bp-md) {
    margin-top: 80px;
  }
}
</style>
