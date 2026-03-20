<template>
  <main id="main-content" role="main">
    <TheIntro />
    <TheFindLei />
    <TheWhyUs />
    <TheTrustedBy />
    <TheCustomerStories />
    <TheTestimonials />
    <TheFrameworks />
    <TheCustomerProfiles />
    <TheNewsInsights />
    <ContactInquiryModal />
  </main>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDialog } from 'primevue/usedialog';
import SessionDialog from '@/components/general/SessionDialog.vue';
import { SessionDialogMode } from '@/utils/SessionTimeoutUtils';

import TheIntro from '@/components/resources/landingPage/TheIntro.vue';
import TheFindLei from '@/components/resources/landingPage/TheFindLei.vue';
import TheWhyUs from '@/components/resources/landingPage/TheWhyUs.vue';
import TheTrustedBy from '@/components/resources/landingPage/TheTrustedBy.vue';
import TheCustomerStories from '@/components/resources/landingPage/TheCustomerStories.vue';
import TheTestimonials from '@/components/resources/landingPage/TheTestimonials.vue';
import TheFrameworks from '@/components/resources/landingPage/TheFrameworks.vue';
import TheCustomerProfiles from '@/components/resources/landingPage/TheCustomerProfiles.vue';
import TheNewsInsights from '@/components/resources/landingPage/TheNewsInsights.vue';
import ContactInquiryModal from '@/components/generics/ContactInquiryModal.vue';

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
