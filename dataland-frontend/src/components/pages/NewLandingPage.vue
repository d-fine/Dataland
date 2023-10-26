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

import TheHeader from "@/components/generics/TheNewHeader.vue";
import TheIntro from "@/components/resources/newLandingPage/TheIntro.vue";
import TheQuotes from "@/components/resources/newLandingPage/TheQuotes.vue";
import TheBrands from "@/components/resources/newLandingPage/TheBrands.vue";
import TheStruggle from "@/components/resources/newLandingPage/TheStruggle.vue";
import TheHowItWorks from "@/components/resources/newLandingPage/TheHowItWorks.vue";
import TheJoinCampaign from "@/components/resources/newLandingPage/TheJoinCampaign.vue";
import TheGetInTouch from "@/components/resources/newLandingPage/TheGetInTouch.vue";
import TheFooter from "@/components/generics/TheNewFooter.vue";
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
  const script = document.createElement("script");
  script.id = "Cookiebot";
  script.src = "https://consent.cookiebot.com/uc.js";
  script.setAttribute("data-cbid", "cba5002e-6f0e-4848-aadc-ccc8d5c96c86");
  script.setAttribute("data-blockingmode", "auto");
  script.type = "text/javascript";
  document.head.appendChild(script);
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
