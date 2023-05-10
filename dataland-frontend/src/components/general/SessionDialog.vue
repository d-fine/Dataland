<template>
  <h1>{{ displayedHeader }}</h1>
  <span>{{ displayedText }}</span>
  <div class="mt-5 flex flex-row-reverse flex-wrap">
    <PrimeButton
      v-if="sessionDialogMode === SessionDialogMode.ExternalLogout"
      label="Login to preview account"
      class="p-button-sm uppercase d-letters w-15rem"
      name="login_dataland_button_on_session_modal"
      @click="login"
    />
    <PrimeButton
      v-if="
        sessionDialogMode === SessionDialogMode.SessionWarning || sessionDialogMode === SessionDialogMode.SessionClosed
      "
      :label="sessionDialogMode === SessionDialogMode.SessionClosed ? 'Login' : 'Refresh Session'"
      class="p-button-sm uppercase d-letters w-15rem"
      name="refresh_session_button"
      @click="handleRefreshSession"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import PrimeButton from "primevue/button";
import {
  isRefreshTokenExpiryTimestampInSharedStoreReached,
  SessionDialogMode,
  tryToRefreshSession,
} from "@/utils/SessionTimeoutUtils";
import { TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS } from "@/utils/Constants";
import { useSharedSessionStateStore } from "@/stores/stores";
import { KeycloakComponentSetup, loginAndRedirectToSearchPage } from "@/utils/KeycloakUtils";

export default defineComponent({
  inject: ["dialogRef"],
  name: "SessionTimeoutModal",
  components: { PrimeButton },

  setup() {
    return KeycloakComponentSetup;
  },

  data() {
    return {
      sessionDialogMode: undefined as undefined | number,
      SessionDialogMode,
      functionIdOfExpiryCheck: undefined as undefined | number,
    };
  },

  watch: {
    currentRefreshTokenInSharedStore() {
      this.closeTheDialog();
      if (this.sessionDialogMode === SessionDialogMode.ExternalLogout) {
        void this.$router.push({ path: "/companies", replace: true });
      }
    },
  },

  computed: {
    currentRefreshTokenInSharedStore(): string | undefined {
      return useSharedSessionStateStore().refreshToken ?? undefined;
    },
    displayedHeader(): string | undefined {
      switch (this.sessionDialogMode) {
        case SessionDialogMode.SessionWarning:
          return "Session expires soon";
        case SessionDialogMode.SessionClosed:
          return "Session closed";
        case SessionDialogMode.ExternalLogout:
          return "You have been logged out";
        default:
          return "";
      }
    },

    displayedText(): string | undefined {
      switch (this.sessionDialogMode) {
        case SessionDialogMode.SessionWarning:
          return "To refresh it, please click on the button below.";
        case SessionDialogMode.SessionClosed:
          return "Your session has been closed due to inactivity. Login to start a new session.";
        case SessionDialogMode.ExternalLogout:
          return "Do you want to login again?";
        default:
          return "";
      }
    },
  },

  mounted() {
    this.getDataFromParentAndSet();
    if (this.sessionDialogMode === SessionDialogMode.SessionWarning) {
      this.setIntervalForRefreshTokenExpiryCheck();
    }
  },

  beforeUnmount() {
    if (this.functionIdOfExpiryCheck) {
      clearInterval(this.functionIdOfExpiryCheck);
    }
  },

  methods: {
    /**
     * Sends the user to the keycloak login page
     */
    login() {
      this.getKeycloakPromise!()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            loginAndRedirectToSearchPage(keycloak);
          }
        })
        .catch((error) => console.log(error));
    },

    /**
     * Handles a click on the refresh button.
     */
    handleRefreshSession() {
      this.getKeycloakPromise!()
        .then((keycloak) => {
          if (keycloak.authenticated) {
            tryToRefreshSession(keycloak);
            this.closeTheDialog();
          }
        })
        .catch((error) => console.log(error));
    },

    /**
     * Closes the dialog.
     */
    closeTheDialog() {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      dialogRefToDisplay.close();
    },

    /**
     * Gets all the data that is passed down by the component which has opened this modal and stores it in the
     * component-level data object.
     */
    getDataFromParentAndSet() {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      const dialogRefData = dialogRefToDisplay.data as {
        sessionDialogMode: number;
      };
      this.sessionDialogMode = dialogRefData.sessionDialogMode;
    },

    /**
     * Starts a setInterval-method which - in fixed time intervals - checks if the refresh token expiry timestamp has
     * been surpassed.
     * If it has been surpassed, the text in the modal changes to inform the user that the session can be considered
     * closed. It also clears this setInterval method.
     * Else it keeps going.
     */
    setIntervalForRefreshTokenExpiryCheck() {
      this.functionIdOfExpiryCheck = setInterval(() => {
        if (isRefreshTokenExpiryTimestampInSharedStoreReached()) {
          this.sessionDialogMode = SessionDialogMode.SessionClosed;
          clearInterval(this.functionIdOfExpiryCheck);
        }
      }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
    },
  },
});
</script>
