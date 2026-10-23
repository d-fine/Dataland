<template>
  <DynamicDialog />
  <component
    v-if="route.meta.requiresAuthentication !== undefined"
    :is="route.meta.requiresAuthentication ? AuthenticationWrapper : 'div'"
  >
    <TheHeader />
    <router-view />
  </component>
  <TheFooter />
  <VueQueryDevtools />
</template>

<script setup lang="ts">
import { computed, onBeforeMount, onMounted, onUnmounted, provide, ref, watch } from 'vue';
import DynamicDialog from 'primevue/dynamicdialog';
import Keycloak from 'keycloak-js';
import { logoutAndRedirectToUri } from '@/utils/KeycloakUtils';
import {
  SessionDialogMode,
  startSessionSetIntervalFunctionAndReturnItsId,
  updateTokenAndItsExpiryTimestampAndStoreBoth,
} from '@/utils/SessionTimeoutUtils';
import SessionDialog from '@/components/general/SessionDialog.vue';
import { KEYCLOAK_INIT_OPTIONS } from '@/utils/Constants';
import { useSharedSessionStateStore } from '@/stores/Stores';
import { ApiClientProvider } from '@/services/ApiClients';
import { type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { getCompanyRoleAssignmentsForCurrentUser } from '@/utils/CompanyRolesUtils';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { useRoute } from 'vue-router';

import TheHeader from '@/components/generics/TheHeader.vue';
import TheFooter from '@dataland/shared-elements/footer';
import { useDialog } from 'primevue/usedialog';
import { VueQueryDevtools } from '@tanstack/vue-query-devtools';

const smallScreenBreakpoint = 768;
const windowWidth = ref<number>();
const storeWindowWidth = (): void => {
  windowWidth.value = globalThis.innerWidth;
};

const route = useRoute();
const dialog = useDialog();

const keycloakPromise = ref<Promise<Keycloak>>();
const resolvedKeycloakPromise = ref<Keycloak>();
const keycloakAuthenticated = ref(false);
const functionIdOfSessionSetInterval = ref<number>();
const apiClientProvider = ref<ApiClientProvider>();
const companyRoleAssignments = ref<Array<CompanyRoleAssignmentExtended>>();

const currentRefreshTokenInSharedStore = computed(() => useSharedSessionStateStore().refreshToken);

watch(currentRefreshTokenInSharedStore, (newRefreshToken) => {
  if (resolvedKeycloakPromise.value && newRefreshToken) {
    resolvedKeycloakPromise.value.refreshToken = newRefreshToken;
    if (functionIdOfSessionSetInterval.value) {
      clearInterval(functionIdOfSessionSetInterval.value);
    }
    functionIdOfSessionSetInterval.value = startSessionSetIntervalFunctionAndReturnItsId(
      resolvedKeycloakPromise.value,
      openSessionWarningModal
    );
  }
});

provide('getKeycloakPromise', (): Promise<Keycloak> => {
  if (keycloakPromise.value) return keycloakPromise.value;
  throw new Error('The Keycloak promise has not yet been initialised. This should not be possible...');
});
provide(
  'companyRoleAssignments',
  computed(() => companyRoleAssignments.value)
);
provide(
  'authenticated',
  computed(() => keycloakAuthenticated.value)
);
provide(
  'apiClientProvider',
  computed(() => apiClientProvider.value)
);
provide(
  'useMobileView',
  computed(() => (windowWidth?.value ?? globalThis.innerWidth) <= smallScreenBreakpoint)
);

onBeforeMount(() => {
  processUserAuthentication();
});
onMounted(() => {
  globalThis.addEventListener('resize', storeWindowWidth);
});
onUnmounted(() => {
  globalThis.removeEventListener('resize', storeWindowWidth);
});

/**
 * Sets up the whole authentication status of the user when starting the Dataland Frontend App.
 */
function processUserAuthentication(): void {
  keycloakPromise.value = initKeycloak();
  const provider = new ApiClientProvider(keycloakPromise.value);
  apiClientProvider.value = provider;
  keycloakPromise.value
    .then((keycloak) => handleResolvedKeycloakPromise(keycloak, provider))
    .catch((e) => console.log(e));
}

/**
 * Initializes the Keycloak adaptor and configures it according to the requirements of the Dataland application.
 * @returns a promise which resolves to the Keycloak adaptor object
 */
function initKeycloak(): Promise<Keycloak> {
  const keycloak = new Keycloak(KEYCLOAK_INIT_OPTIONS);
  keycloak.onAuthLogout = handleAuthLogout;
  return keycloak
    .init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: globalThis.location.origin + '/static/silent-check-sso.html',
      pkceMethod: 'S256',
    })
    .then((authenticated) => {
      keycloakAuthenticated.value = authenticated;
    })
    .catch((error) => {
      console.log('Error in init Keycloak ', error);
      keycloakAuthenticated.value = false;
    })
    .then((): Keycloak => keycloak);
}

/**
 * Executes actions based on the resolved Keycloak-login-status of the current user
 * @param resolved contains the login-status of the current user
 * @param provider to trigger a request to the backend of Dataland for getting the users company roles
 */
function handleResolvedKeycloakPromise(resolved: Keycloak, provider: ApiClientProvider): void {
  resolvedKeycloakPromise.value = resolved;
  if (resolved.authenticated) {
    localStorage.setItem('dataland_authenticated', 'true');
    void updateTokenAndItsExpiryTimestampAndStoreBoth(resolved, true);
    void setCompanyRolesForUser(resolved, provider);
  } else {
    localStorage.removeItem('dataland_authenticated');
  }
}

/**
 * Fetches the company roles of the current user and stores it in a variable.
 * @param resolved contains the login-status of the current user
 * @param provider to trigger a request to the backend of Dataland for getting the users company roles
 */
async function setCompanyRolesForUser(resolved: Keycloak, provider: ApiClientProvider): Promise<void> {
  companyRoleAssignments.value = await getCompanyRoleAssignmentsForCurrentUser(resolved, provider);
}

/**
 * Executed as callback when the user is logged out. Redirects the user to the Astro website at /.
 */
function handleAuthLogout(): void {
  localStorage.removeItem('dataland_authenticated');
  logoutAndRedirectToUri(resolvedKeycloakPromise.value as Keycloak, '?externalLogout=true');
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
