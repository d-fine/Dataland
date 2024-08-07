<template>
  <DynamicDialog />
  <router-view />
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue';
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

const smallScreenBreakpoint = 768;
const windowWidth = ref<number>();
const storeWindowWidth = (): void => {
  windowWidth.value = window.innerWidth;
};

export default defineComponent({
  name: 'app',
  components: { DynamicDialog },

  data() {
    return {
      keycloakPromise: undefined as undefined | Promise<Keycloak>,
      apiClientProvider: undefined as ApiClientProvider | undefined,
      resolvedKeycloakPromise: undefined as undefined | Keycloak,
      keycloakAuthenticated: false,
      functionIdOfSessionSetInterval: undefined as number | undefined,
    };
  },

  watch: {
    currentRefreshTokenInSharedStore(newRefreshToken: string) {
      if (this.resolvedKeycloakPromise && newRefreshToken) {
        this.resolvedKeycloakPromise.refreshToken = newRefreshToken;
        clearInterval(this.functionIdOfSessionSetInterval);
        const openSessionWarningModalBound = this.openSessionWarningModal.bind(this);
        this.functionIdOfSessionSetInterval = startSessionSetIntervalFunctionAndReturnItsId(
          this.resolvedKeycloakPromise,
          openSessionWarningModalBound
        );
      }
    },
  },

  computed: {
    currentRefreshTokenInSharedStore() {
      return useSharedSessionStateStore().refreshToken;
    },
  },

  created() {
    this.keycloakPromise = this.initKeycloak();
    this.keycloakPromise
      .then((keycloak) => {
        this.resolvedKeycloakPromise = keycloak;
        if (this.resolvedKeycloakPromise?.authenticated) {
          void updateTokenAndItsExpiryTimestampAndStoreBoth(this.resolvedKeycloakPromise, true);
        }
      })
      .catch((e) => console.log(e));

    this.apiClientProvider = new ApiClientProvider(this.keycloakPromise);
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
      useMobileView: computed(() => (windowWidth?.value ?? window.innerWidth) <= smallScreenBreakpoint),
    };
  },

  mounted() {
    window.addEventListener('resize', storeWindowWidth);
  },
  unmounted() {
    window.removeEventListener('resize', storeWindowWidth);
  },

  methods: {
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
          silentCheckSsoRedirectUri: window.location.origin + '/static/silent-check-sso.html',
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
     * Executed as callback when the user is logged out. It tries another logout by redirecting the user to a keycloak
     * logout uri, where the user is instantly re-redirected back to the Welcome page with a specific query param
     * in the url which triggers a pop-up to open and inform the user that she/he was just logged out.
     */
    handleAuthLogout() {
      logoutAndRedirectToUri(this.resolvedKeycloakPromise as Keycloak, '?externalLogout=true');
    },

    /**
     * Opens a pop-up to warn the user that the session will expire soon and offers a button to refresh it.
     * If the refresh button is clicked soon enough, the session is refreshed.
     * Else the text changes and tells the user that the session was closed.
     */
    openSessionWarningModal(): void {
      this.$dialog.open(SessionDialog, {
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
