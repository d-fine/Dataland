<template>
  <DynamicDialog />
  <router-view />
</template>

<script setup lang="ts">
import { ref, watch, computed, provide } from "vue";
import DynamicDialog from "primevue/dynamicdialog";
import { useDialog } from "primevue/usedialog";
import Keycloak from "keycloak-js";
import { logoutAndRedirectToUri } from "@/utils/KeycloakUtils";
import {
  SessionDialogMode,
  startSessionSetIntervalFunctionAndReturnItsId,
  updateTokenAndItsExpiryTimestampAndStoreBoth,
} from "@/utils/SessionTimeoutUtils";
import SessionDialog from "@/components/general/SessionDialog.vue";
import { KEYCLOAK_INIT_OPTIONS } from "@/utils/Constants";
import { useSharedSessionStateStore } from "@/stores/Stores";
const sharedStore = useSharedSessionStateStore();
const keycloakPromise = ref<Promise<Keycloak> | undefined>();
const resolvedKeycloakPromise = ref<Keycloak | undefined>();
const keycloakAuthenticated = ref(false);
const functionIdOfSessionSetInterval = ref<number | undefined>();
const dialog = useDialog();

const currentRefreshTokenInSharedStore = computed(() => sharedStore.refreshToken);

watch(currentRefreshTokenInSharedStore, (newRefreshToken) => {
  if (typeof newRefreshToken === "string") {
    //try is to explicitly declare the type for newRefreshToken inside the callback
    if (resolvedKeycloakPromise.value && newRefreshToken) {
      resolvedKeycloakPromise.value.refreshToken = newRefreshToken;
      clearInterval(functionIdOfSessionSetInterval.value);
      functionIdOfSessionSetInterval.value = startSessionSetIntervalFunctionAndReturnItsId(
        resolvedKeycloakPromise.value,
        openSessionWarningModal,
      );
    }
  }
});

keycloakPromise.value = initKeycloak();
keycloakPromise.value
  .then((keycloak) => {
    resolvedKeycloakPromise.value = keycloak;
    if (resolvedKeycloakPromise.value?.authenticated) {
      updateTokenAndItsExpiryTimestampAndStoreBoth(resolvedKeycloakPromise.value, true);
    }
  })
  .catch((e) => console.log(e));

provide("getKeycloakPromise", () => {
  if (keycloakPromise.value) return keycloakPromise.value;
  throw new Error("The Keycloak promise has not been initialized. This should not be possible...");
});
provide(
  "authenticated",
  computed(() => keycloakAuthenticated.value),
);

/**
 * Initializes the Keycloak adaptor and configures it according to the requirements of the Dataland application.
 * @returns a promise which resolves to the Keycloak adaptor object
 */
function initKeycloak(): Promise<Keycloak> {
  const keycloak = new Keycloak(KEYCLOAK_INIT_OPTIONS);
  keycloak.onAuthLogout = handleAuthLogout;
  return keycloak
    .init({
      onLoad: "check-sso",
      silentCheckSsoRedirectUri: window.location.origin + "/static/silent-check-sso.html",
      pkceMethod: "S256",
    })
    .then((authenticated) => {
      keycloakAuthenticated.value = authenticated;
      return keycloak;
    })
    .catch((error) => {
      console.log("Error in init Keycloak ", error);
      keycloakAuthenticated.value = false;
      return keycloak;
    });
}
/**
 * Executed as callback when the user is logged out. It tries another logout by redirecting the user to a keycloak
 * logout uri, where the user is instantly re-redirected back to the Welcome page with a specific query param
 * in the url which triggers a pop-up to open and inform the user that she/he was just logged out.
 */
function handleAuthLogout(): void {
  logoutAndRedirectToUri(resolvedKeycloakPromise.value as Keycloak, "?externalLogout=true");
}

/**
 * Opens a pop-up to warn the user that the session will expire soon and offers a button to refresh it.
 * If the refresh button is clicked soon enough, the session is refreshed.
 * Else the text changes and tells the user that the session was closed.
 */
function openSessionWarningModal(): void {
  dialog.open(SessionDialog, {
    props: {
      modal: true,
      closable: false,
      closeOnEscape: false,
      showHeader: false,
    },
    data: {
      sessionDialogMode: SessionDialogMode.SessionWarning,
    },
  });
}
</script>
