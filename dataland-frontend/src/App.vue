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
import { type CompanyRoleAssignment } from '@clients/communitymanager';
import { getCompanyRoleAssignmentsForCurrentUser } from '@/utils/CompanyRolesUtils';

const smallScreenBreakpoint = 768;
const windowWidth = ref<number>();
const storeWindowWidth = (): void => {
  windowWidth.value = window.innerWidth;
};
// TODO detekt faiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiil
export default defineComponent({
  name: 'app',
  components: { DynamicDialog },

  data() {
    return {
      keycloakPromise: undefined as undefined | Promise<Keycloak>,
      resolvedKeycloakPromise: undefined as undefined | Keycloak,
      keycloakAuthenticated: false,
      functionIdOfSessionSetInterval: undefined as number | undefined,

      apiClientProvider: undefined as ApiClientProvider | undefined,

      companyRoleAssignments: undefined as Array<CompanyRoleAssignment> | undefined,
    };
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
      companyRoleAssignments: computed(() => {
        console.log('Currently the company role assignments are: ' + this.companyRoleAssignments);
        return this.companyRoleAssignments;
      }),
      authenticated: computed(() => {
        return this.keycloakAuthenticated;
      }),
      apiClientProvider: computed(() => {
        return this.apiClientProvider;
      }),
      useMobileView: computed(() => (windowWidth?.value ?? window.innerWidth) <= smallScreenBreakpoint),
    };
  },

  created() {
    this.processUserAuthentication();
  },

  mounted() {
    window.addEventListener('resize', storeWindowWidth);
  },
  unmounted() {
    window.removeEventListener('resize', storeWindowWidth);
  },

  methods: {
    /**
     * Sets up the whole authentication status of the user when starting the Dataland Frontend App.
     */
    processUserAuthentication() {
      this.keycloakPromise = this.initKeycloak();
      if (this.keycloakPromise) {
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
     * Executes actions based on the resolved Keycloak-login-status of the current user
     * @param resolvedKeycloakPromise contains the login-status of the current user
     */
    handleResolvedKeycloakPromise(resolvedKeycloakPromise: Keycloak) {
      this.resolvedKeycloakPromise = resolvedKeycloakPromise;
      if (this.resolvedKeycloakPromise.authenticated) {
        void updateTokenAndItsExpiryTimestampAndStoreBoth(this.resolvedKeycloakPromise, true);
        if (this.apiClientProvider) {
          this.setCompanyRolesForUser(resolvedKeycloakPromise, this.apiClientProvider);
        }
      }
    },

    /**
     * Fetches the company roles of the current user and stores it in a variable.
     * @param resolvedKeycloakPromise contains the login-status of the current user
     * @param apiClientProvider is used to trigger a request to the backend of Dataland
     */
    setCompanyRolesForUser(resolvedKeycloakPromise: Keycloak, apiClientProvider: ApiClientProvider) {
      getCompanyRoleAssignmentsForCurrentUser(resolvedKeycloakPromise, apiClientProvider).then(
        (retrievedCompanyRoleAssignments) => (this.companyRoleAssignments = retrievedCompanyRoleAssignments)
      );
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
