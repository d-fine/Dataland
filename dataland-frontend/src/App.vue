<template>
  <div class="col-10">
    <h4 class="top-50 fixed" style="z-index: 20" data-test="123hallo">
      {{ currentTokenSliced + "---and:  " + currentRefreshTokenInStore?.slice(-20) }}
    </h4>
  </div>
  <DynamicDialog />
  <router-view />
</template>

<script lang="ts">
import Keycloak from "keycloak-js";
import DynamicDialog from "primevue/dynamicdialog";
import { computed, defineComponent } from "vue";
import { logoutAndRedirectToUri } from "@/utils/KeycloakUtils";
import {
  startSessionSetIntervalFunction,
  tryToRefreshSession,
   updateTokenAndItsExpiryTimestampAndStoreBoth,
} from "@/utils/SessionTimeoutUtils";
import SessionDialog from "@/components/general/SessionDialog.vue";
import { KEYCLOAK_INIT_OPTIONS } from "@/utils/Constants";
import { useFunctionIdsStore, useSessionStateStore } from "@/stores/stores";

export default defineComponent({
  name: "app",
  components: { DynamicDialog },

  data() {
    return {
      keycloakPromise: undefined as undefined | Promise<Keycloak>,
      resolvedKeycloakPromise: undefined as undefined | Keycloak,
      keycloakAuthenticated: false,
      currentTokenSliced: useSessionStateStore().refreshToken, // TODO remove at the end
    };
  },

  watch: {
    currentRefreshTokenInStore(newRefreshToken) {
      console.log("NOTE: session store token changed!  update warning timestamp and restart session!");
      if (this.resolvedKeycloakPromise && newRefreshToken) {
        console.log("NOTE2: new refresh token is actually defined, so Dataland is reacting to the change...")
        this.resolvedKeycloakPromise.refreshToken = newRefreshToken;
        this.currentTokenSliced = newRefreshToken.slice(-20); // TODO debugging
        clearInterval(useFunctionIdsStore().functionIdOfSetIntervalForSessionWarning);
        startSessionSetIntervalFunction(this.resolvedKeycloakPromise, this.openSessionWarningModal);
      }
    },
  },

  computed: {
    currentRefreshTokenInStore() {
      return useSessionStateStore().refreshToken;
    },
  },

  async created() {
    this.keycloakPromise = this.initKeycloak();
    this.resolvedKeycloakPromise = await this.keycloakPromise;
    if (this.resolvedKeycloakPromise && this.resolvedKeycloakPromise.authenticated) {
      updateTokenAndItsExpiryTimestampAndStoreBoth(this.resolvedKeycloakPromise)
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
      keycloak.onAuthRefreshError = () => {
        console.log("ERROR!!!: refresherror");
      }; // TODO debugging
      keycloak.onAuthError = () => {
        console.log("ERROR!!!: autherror");
      }; // TODO debugging
      keycloak.onAuthSuccess = () => {
        console.log("SUCCESS: auth!") // TODO debugging
      }
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
     * Opens a pop-up to warn the user that the session will expire soon and offers a button to refresh it.
     * If the refresh button is  clicked or the pop-up is closed soon enough, the session is refreshed.
     * Else the text changes and tells the user that the session was closed. That behaviour is activated in the
     * SessionDialog via the variable isTrackingOfRefreshTokenExpiryEnabled.
     */
    openSessionWarningModal(): void {
      this.$dialog.open(SessionDialog, {
        props: {
          modal: true,
          closable: false,
          closeOnEscape: false
        },
        data: {
          displayedText: "Your session in this tab will expire soon. Please refresh it if you want to stay logged in.",
          showRefreshButton: true,
          isTrackingOfRefreshTokenExpiryEnabled: true,
          resolvedKeycloakPromise: this.resolvedKeycloakPromise,
        },
      });
    },
  },
});
</script>
