<template>
  <TheHeader :landingPage="landingPage" />
  <main role="main">
    <TheIntro :sections="landingPage?.sections" />
    <TheQuotes :sections="landingPage?.sections" />
    <TheBrands :sections="landingPage?.sections" />
    <TheStruggle :sections="landingPage?.sections" />
    <TheHowItWorks :sections="landingPage?.sections" />
    <TheJoinCampaign :sections="landingPage?.sections" />
    <TheGetInTouch :sections="landingPage?.sections" />
  </main>
  <TheFooter :sections="landingPage?.sections" />
</template>

<script setup lang="ts">
import { ref, onMounted, watch, inject } from "vue";
import { useRoute, useRouter, type NavigationFailure } from "vue-router";
import { useDialog } from "primevue/usedialog";
import SessionDialog from "@/components/general/SessionDialog.vue";
import type Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { useSharedSessionStateStore } from "@/stores/Stores";
import { SessionDialogMode } from "@/utils/SessionTimeoutUtils";

import TheHeader from "@/components/layout/TheHeader.vue";
import TheIntro from "@/components/general/TheIntro.vue";
import TheQuotes from "@/components/general/TheQuotes.vue";
import TheBrands from "@/components/general/TheBrands.vue";
import TheStruggle from "@/components/general/TheStruggle.vue";
import TheHowItWorks from "@/components/general/TheHowItWorks.vue";
import TheJoinCampaign from "@/components/general/TheJoinCampaign.vue";
import TheGetInTouch from "@/components/general/TheGetInTouch.vue";
import TheFooter from "@/components/layout/TheFooter.vue";

// Import the JSON content and types
import contentData from "@/assets/content.json";
import type { Content, Page } from "@/types/ContentTypes";

const content: Content = contentData;
const landingPage: Page | undefined = content.pages.find((page) => page.url === "/");

const dialog = useDialog();
const injectedAuthenticated = inject<boolean>("authenticated");
const authenticated = ref(injectedAuthenticated);
const getKeycloakPromise = inject<() => Promise<Keycloak>>("getKeycloakPromise");
const route = useRoute();
const router = useRouter();
const store = useSharedSessionStateStore();
const currentRefreshTokenInSharedStore = ref(store.refreshToken);

onMounted(() => {
  if (route.query.externalLogout === "true") {
    openLogoutModal();
  }
  void checkAuthenticatedAndRedirectIfLoggedIn();
});

watch(authenticated, () => {
  void checkAuthenticatedAndRedirectIfLoggedIn();
});

watch(currentRefreshTokenInSharedStore, () => {
  void router.push({ path: "/companies", replace: true });
});

const checkAuthenticatedAndRedirectIfLoggedIn = async (): Promise<void | NavigationFailure | undefined> => {
  const keycloak = await assertDefined(getKeycloakPromise)();
  if (keycloak.authenticated) {
    return router.push({ path: "/companies", replace: true });
  }
  return Promise.resolve();
};

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
      void router.replace("");
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
