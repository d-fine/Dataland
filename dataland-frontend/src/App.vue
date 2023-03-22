<template>
  <DynamicDialog />
  <router-view />
</template>

<script lang="ts">
import Keycloak from "keycloak-js";
import DynamicDialog from "primevue/dynamicdialog";
import { computed, defineComponent } from "vue";
import SessionTimeoutModal from "@/components/general/SessionTimeoutModal.vue";
import { useTimeoutLogoutStore } from "@/stores/stores";
import { logoutAndRedirectToUri } from "@/utils/KeycloakUtils";

export default defineComponent({
  name: "app",
  components: { DynamicDialog },
  data() {
    return {
      keycloakPromise: undefined as undefined | Promise<Keycloak>,
      keycloakAuthenticated: false,
      keycloakInitOptions: {
        realm: "datalandsecurity",
        url: "/keycloak",
        clientId: "dataland-public",
        onLoad: "login-required",
      },
      sessionTimeoutSetIntervalFunctionId: undefined as undefined | number,
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
          console.log("setting the keycloakAuthenticated value to " + authenticated.toString());
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
    handleAuthLogout() {
      console.log("Logging out"); // TODO debugging
      clearInterval(this.sessionTimeoutSetIntervalFunctionId);
      this.openTimeoutModal("You have been logged out. Do you want to login again?", true);
    },

    /**
     * Opens a pop-up and displays the passed text.
     *
     * @param headerText The text in the header of the pop-up
     * @param showLogInButtonAndRedirectToHomeOnClose Decides if log-in button shall be visible and if closing the
     * pop-up shall result in redirecting to the Welcome page
     */
    openTimeoutModal(headerText: string, showLogInButtonAndRedirectToHomeOnClose: boolean): void {
      this.$dialog.open(SessionTimeoutModal, {
        props: {
          header: headerText,
          modal: true,
          dismissableMask: true,
        },
        data: {
          showLogInButton: showLogInButtonAndRedirectToHomeOnClose,
        },
        onClose: () => {
          if (showLogInButtonAndRedirectToHomeOnClose) {
            void this.$router.push("/");
          }
        },
      });
    },
  },

  async created() {
    this.keycloakPromise = this.initKeycloak(this.handleAuthLogout);
    const resolvedKeycloakPromise = await this.keycloakPromise;
    const timerIncrementInMs = 10 * 1000;
    if (resolvedKeycloakPromise && resolvedKeycloakPromise.authenticated) {
      const sessionStateStore = useTimeoutLogoutStore();
      this.sessionTimeoutSetIntervalFunctionId = setInterval(() => {
        sessionStateStore.reduceTimerBySeconds(timerIncrementInMs / 1000);
        console.log(sessionStateStore.remainingSessionTimeInSeconds); // TODO debugging
        if (sessionStateStore.remainingSessionTimeInSeconds === 30) {
          // TODO increase time
          this.openTimeoutModal("Your session is almost over.", false);
        }
        if (sessionStateStore.remainingSessionTimeInSeconds === 0) {
          logoutAndRedirectToUri(resolvedKeycloakPromise, "?timeout=true");
        }
      }, timerIncrementInMs);
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
