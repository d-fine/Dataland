<template>
  <TheHeader :landingPage="landingPage" :contentData="content" />
  <main role="main">test mission page</main>
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

import TheFooter from "@/components/layout/TheFooter.vue";

// Import the JSON content and types
import contentData from "@/assets/content.json";
import type { Content, Page } from "@/types/ContentTypes";

const content: Content = contentData;
const landingPage: Page | undefined = content.pages.find((page) => page.url === "/lp");

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
