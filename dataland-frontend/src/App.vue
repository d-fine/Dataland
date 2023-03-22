<template>
  <DynamicDialog />
  <router-view />
</template>

<script lang="ts">
import Keycloak from "keycloak-js";
import DynamicDialog from "primevue/dynamicdialog";
import { computed, defineComponent } from "vue";
import { logoutAndRedirectToUri } from "@/utils/KeycloakUtils";
import {
  getSessionTimeoutTimestampFromLocalStorage,
  resetSessionTimeoutTimestampInLocalStorage,
} from "@/utils/SessionTimeoutUtils";

export default defineComponent({
  name: "app",
  components: { DynamicDialog },
  data() {
    return {
      keycloakPromise: undefined as undefined | Promise<Keycloak>,
      resolvedKeycloakPromise: undefined as undefined | Keycloak,
      keycloakAuthenticated: false,
      keycloakInitOptions: {
        realm: "datalandsecurity",
        url: "/keycloak",
        clientId: "dataland-public",
        onLoad: "login-required",
      },
      timeDistanceBetweenSetIntervalExecutionsInMs: 5000 as number,
    };
  },
  methods: {
    /**
     * Initializes the Keycloak adaptor and configures it according to the requirements of the Dataland application.
     *
     * @param authLogoutCallback The callback function to execute on auth logouts.
     * @returns a promise which resolves to the Keycloak adaptor object
     */
    initKeycloak(authLogoutCallback: () => void): Promise<Keycloak> {
      const keycloak = new Keycloak(this.keycloakInitOptions);
      keycloak.onAuthLogout = authLogoutCallback;
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
     * Executed as callback when the user is logged out: It stops the setInterval-function which monitors the duration
     * of the current session and then opens a pop-up to show the user that she/he has been logged out.
     *
     */
    async handleAuthLogout() {
      console.log("Logging out"); // TODO debugging
      logoutAndRedirectToUri(this.resolvedKeycloakPromise as Keycloak, "?externalLogout=true");
    },
  },

  async created() {
    this.keycloakPromise = this.initKeycloak(this.handleAuthLogout);
    this.resolvedKeycloakPromise = await this.keycloakPromise;
    if (this.resolvedKeycloakPromise && this.resolvedKeycloakPromise.authenticated) {
      resetSessionTimeoutTimestampInLocalStorage();
      setInterval(() => {
        console.log("setInterval is running once"); // TODO debugging
        const currentTimestampInMs = new Date().getTime();
        const logoutTimeStampInLocalStorage = getSessionTimeoutTimestampFromLocalStorage();
        if (!logoutTimeStampInLocalStorage) {
          logoutAndRedirectToUri(this.resolvedKeycloakPromise as Keycloak, "");
        } else {
          if (currentTimestampInMs >= logoutTimeStampInLocalStorage) {
            console.log("You have passed the logout timestamp. You'll be logged out now"); // TODO debugging
            logoutAndRedirectToUri(this.resolvedKeycloakPromise as Keycloak, "?timeout=true");
          } else {
            console.log("You have not reached the logoutTimeStamp in the local storage yet. You stay logged in"); // TODO debugging
          }
        }
      }, this.timeDistanceBetweenSetIntervalExecutionsInMs);
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
});
</script>
