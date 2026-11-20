<template>
  <DynamicDialog />
  <AuthenticationWrapper
    v-if="route.meta.requiresAuthentication !== undefined"
    :disable-authentication-wrapper="!route.meta.requiresAuthentication"
  >
    <TheHeader />
    <router-view />
  </AuthenticationWrapper>
  <TheFooter />
  <VueQueryDevtools />
</template>

<script lang="ts">
import { type Component, computed, defineComponent, ref } from 'vue';
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
export default defineComponent({
  name: 'app',
  components: {
    TheHeader,
    DynamicDialog,
    AuthenticationWrapper,
    TheFooter,
    // Cast needed: its inferred props type references the non-exported DevtoolsOptions (TS2883)
    VueQueryDevtools: VueQueryDevtools as Component,
  },

  data() {
    return {
      keycloakPromise: undefined as undefined | Promise<Keycloak>,
      resolvedKeycloakPromise: undefined as undefined | Keycloak,
      keycloakAuthenticated: false,
      functionIdOfSessionSetInterval: undefined as number | undefined,

      apiClientProvider: undefined as ApiClientProvider | undefined,
    };
  },

  setup() {
    const route = useRoute();
    const dialog = useDialog();
    return { route, dialog };
  },

  computed: {
    currentRefreshTokenInSharedStore() {
      return useSharedSessionStateStore().refreshToken;
    },
  },

  watch: {
    currentRefreshTokenInSharedStore(newRefreshToken: string) {
      if (this.resolvedKeycloakPromise && newRefreshToken) {
        this.resolvedKeycloakPromise.refreshToken = newRefreshToken;
        if (this.functionIdOfSessionSetInterval) {
          clearInterval(this.functionIdOfSessionSetInterval);
        }
        const openSessionWarningModalBound = this.openSessionWarningModal.bind(this);
        this.functionIdOfSessionSetInterval = startSessionSetIntervalFunctionAndReturnItsId(
          this.resolvedKeycloakPromise,
          openSessionWarningModalBound
        );
      }
    },
  },

  provide() {
    return {
      getKeycloakPromise: (): Promise<Keycloak> => {
        if (this.keycloakPromise) return this.keycloakPromise;
        throw new Error('The Keycloak promise has not yet been initialised. This should not be possible...');
      },
      authenticated: computed(() => {
        return this.keycloakAuthenticated;
      }),
      apiClientProvider: computed(() => {
        return this.apiClientProvider;
      }),
      useMobileView: computed(() => (windowWidth?.value ?? globalThis.innerWidth) <= smallScreenBreakpoint),
    };
  },

  created() {
    this.processUserAuthentication();
  },

  mounted() {
    globalThis.addEventListener('resize', storeWindowWidth);
  },
  unmounted() {
    globalThis.removeEventListener('resize', storeWindowWidth);
  },

  methods: {
    /**
     * Sets up the whole authentication status of the user when starting the Dataland Frontend App.
     */
    processUserAuthentication() {
      this.keycloakPromise = this.initKeycloak();
      if (this.keycloakPromise !== undefined) {
        this.apiClientProvider = new ApiClientProvider(this.keycloakPromise);
        this.keycloakPromise
          .then((keycloak) => this.handleResolvedKeycloakPromise(keycloak))
          .catch((e) => console.log(e));
      }
    },
    /**
     * Initializes the Keycloak adaptor and configures it according to the requirements of the Dataland application.
     * @returns a promise which resolves to the Keycloak adaptor object
     */
    initKeycloak(): Promise<Keycloak> {
      const keycloak = new Keycloak(KEYCLOAK_INIT_OPTIONS);
      keycloak.onAuthLogout = this.handleAuthLogout.bind(this);
      return keycloak
        .init({
          onLoad: 'check-sso',
          silentCheckSsoRedirectUri: globalThis.location.origin + '/static/silent-check-sso.html',
          pkceMethod: 'S256',
        })
        .then((authenticated) => {
          this.keycloakAuthenticated = authenticated;
        })
        .catch((error) => {
          console.log('Error in init Keycloak ', error);
          this.keycloakAuthenticated = false;
        })
        .then((): Keycloak => {
          return keycloak;
        });
    },

    /**
     * Executes actions based on the resolved Keycloak-login-status of the current user
     * @param resolvedKeycloakPromise contains the login-status of the current user
     */
    handleResolvedKeycloakPromise(resolvedKeycloakPromise: Keycloak) {
      this.resolvedKeycloakPromise = resolvedKeycloakPromise;
      if (this.resolvedKeycloakPromise.authenticated) {
        localStorage.setItem('dataland_authenticated', 'true');
        void updateTokenAndItsExpiryTimestampAndStoreBoth(this.resolvedKeycloakPromise, true);
      } else {
        localStorage.removeItem('dataland_authenticated');
      }
    },

    /**
     * Executed as callback when the user is logged out. Redirects the user to the Astro website at /.
     */
    handleAuthLogout() {
      localStorage.removeItem('dataland_authenticated');
      logoutAndRedirectToUri(this.resolvedKeycloakPromise as Keycloak, '?externalLogout=true');
    },

    /**
     * Opens a pop-up to warn the user that the session will expire soon and offers a button to refresh it.
     * If the refresh button is clicked soon enough, the session is refreshed.
     * Else the text changes and tells the user that the session was closed.
     */
    openSessionWarningModal(): void {
      this.dialog.open(SessionDialog, {
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
    },
  },
});
</script>
