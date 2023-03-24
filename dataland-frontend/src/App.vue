<template>
  <DynamicDialog />
  <router-view />
</template>

<script lang="ts">
import Keycloak from "keycloak-js";
import DynamicDialog from "primevue/dynamicdialog";
import { computed, defineComponent } from "vue";
import { logoutAndRedirectToUri } from "@/utils/KeycloakUtils";
import { clearSessionSetIntervalFunction, tryToRefreshSession } from "@/utils/SessionTimeoutUtils";
import SessionTimeoutModal from "@/components/general/SessionTimeoutModal.vue";
import { KEYCLOAK_INIT_OPTIONS } from "@/utils/Constants";

export default defineComponent({
  name: "app",
  components: { DynamicDialog },

  data() {
    return {
      keycloakPromise: undefined as undefined | Promise<Keycloak>,
      resolvedKeycloakPromise: undefined as undefined | Keycloak,
      keycloakAuthenticated: false,
    };
  },

  async created() {
    this.keycloakPromise = this.initKeycloak();
    this.resolvedKeycloakPromise = await this.keycloakPromise;
    if (this.resolvedKeycloakPromise && this.resolvedKeycloakPromise.authenticated) {
      tryToRefreshSession(this.resolvedKeycloakPromise as Keycloak, this.handleSurpassingTheExpiredSessionTimestamp);
    }
  },
  provide() {
    return {
      getKeycloakPromise: (): Promise<Keycloak> => {
        if (this.keycloakPromise) return this.keycloakPromise;
        throw new Error("The keycloak promise has not yet been initialised. This should not be possible...");
      },
      authenticated: computed(() => {
        return this.keycloakAuthenticated;
      }),
    };
  },

  methods: {
    /**
     * Initializes the Keycloak adaptor and configures it according to the requirements of the Dataland application.
     *
     * @returns a promise which resolves to the Keycloak adaptor object
     */
    initKeycloak(): Promise<Keycloak> {
      const keycloak = new Keycloak(KEYCLOAK_INIT_OPTIONS);
      keycloak.onAuthLogout = this.handleAuthLogout;
      keycloak.onAuthRefreshSuccess = () => {
        console.log("refreshed tokens");
      }; // TODO debugging
      return keycloak
        .init({
          onLoad: "check-sso",
          silentCheckSsoRedirectUri: window.location.origin + "/silent-check-sso.html",
          pkceMethod: "S256",
        })
        .then((authenticated) => {
          console.log("setting the keycloakAuthenticated value to " + authenticated.toString()); // TODO debugging
          this.keycloakAuthenticated = authenticated;
        })
        .catch((error) => {
          console.log("Error in init keycloak ", error);
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
    async handleAuthLogout() {
      console.log("Logging out"); // TODO debugging
      logoutAndRedirectToUri(this.resolvedKeycloakPromise as Keycloak, "?externalLogout=true");
    },

    /**
     * Opens a pop-up window to inform the user about the expired session and stops the setInterval-function which
     * constantly checks for an expired session.
     */
    handleSurpassingTheExpiredSessionTimestamp() {
      this.openSessionWarningModal();
      clearSessionSetIntervalFunction();
    },

    /**
     * Opens a pop-up to warn the user that the session will be closed soon and offers a button to refresh it.
     * If it is closed, it also refreshes the session.
     */
    openSessionWarningModal(): void {
      // TODO write openModal as central function somewhere?
      this.$dialog.open(SessionTimeoutModal, {
        props: {
          header: "Your session will be closed soon. Please refresh it if you want to stay logged in.",
          modal: true,
          dismissableMask: true,
        },
        data: {
          showRefreshButton: true,
        },
        onClose: () => {
          tryToRefreshSession(
            this.resolvedKeycloakPromise as Keycloak,
            this.handleSurpassingTheExpiredSessionTimestamp
          );
        },
      });
    },
  },
});
</script>
