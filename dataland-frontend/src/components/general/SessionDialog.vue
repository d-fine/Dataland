<template>
  <h1>{{ displayedHeader }}</h1>
  <span>{{ displayedText }}</span>
  <div class="mt-5 flex flex-row-reverse flex-wrap">
    <PrimeButton
      v-if="isInExternalLogoutMode"
      label="Login to preview account"
      class="p-button-sm uppercase d-letters w-15rem"
      name="login_dataland_button_on_session_modal"
      @click="login"
    />
    <PrimeButton
      v-if="isInSessionWarningMode || isInSessionClosedMode"
      :label="isInSessionClosedMode ? 'Login' : 'Refresh Session'"
      class="p-button-sm uppercase d-letters w-15rem"
      name="refresh_session_button"
      @click="handleRefreshSession"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import PrimeButton from "primevue/button";
import { isRefreshTokenExpiryTimestampInSharedStoreReached, tryToRefreshSession } from "@/utils/SessionTimeoutUtils";
import Keycloak from "keycloak-js";
import { TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS } from "@/utils/Constants";
import { useSharedSessionStateStore } from "@/stores/stores";
import { loginAndRedirectToSearchPage } from "@/utils/KeycloakUtils";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  inject: ["dialogRef"],
  name: "SessionTimeoutModal",
  components: { PrimeButton },

  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data() {
    return {
      functionIdOfExpiryCheck: undefined as undefined | number,
      isInSessionWarningMode: false,
      isInSessionClosedMode: false,
      isInExternalLogoutMode: false,
    };
  },

  watch: {
    currentRefreshTokenInSharedStore() {
      this.closeTheDialog();
      if (this.isInExternalLogoutMode) {
        void this.$router.push({ path: "/companies", replace: true });
      }
    },
  },

  computed: {
    currentRefreshTokenInSharedStore(): string | undefined {
      return useSharedSessionStateStore().refreshToken || undefined;
    },
    displayedHeader(): string | undefined {
      switch (true) {
        case this.isInSessionWarningMode:
          return "Session expires soon";
        case this.isInSessionClosedMode:
          return "Session closed";
        case this.isInExternalLogoutMode:
          return "You have been logged out";
        default:
          return "";
      }
    },
    displayedText(): string | undefined {
      switch (true) {
        case this.isInSessionWarningMode:
          return "To refresh it, please click on the button below.";
        case this.isInSessionClosedMode:
          return "Your session has been closed due to inactivity. Login to start a new session.";
        case this.isInExternalLogoutMode:
          return "Do you want to login again?";
        default:
          return "";
      }
    },
  },

  mounted() {
    this.getDataFromParentAndSet();
    if (this.isInSessionWarningMode) {
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
      assertDefined(this.getKeycloakPromise)()
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
      assertDefined(this.getKeycloakPromise)()
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
        isInSessionWarningMode: boolean;
        isInExternalLogoutMode: boolean;
      };
      this.isInSessionWarningMode = dialogRefData.isInSessionWarningMode;
      this.isInExternalLogoutMode = dialogRefData.isInExternalLogoutMode;
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
          this.isInSessionWarningMode = false;
          this.isInSessionClosedMode = true;
          clearInterval(this.functionIdOfExpiryCheck);
        }
      }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
    },
  },
});
</script>
