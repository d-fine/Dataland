<template>
  <h1>{{ displayedHeader }}</h1>
  <span>{{ displayedText }}</span>
  <div class="mt-5 flex flex-row-reverse flex-wrap">
    <AuthenticationButton v-if="showLogInButton" :customClassForButton="buttonClass" />
    <PrimeButton
      v-if="showRefreshButton"
      :label="refreshButtonLabel"
      :class="buttonClass"
      name="refresh_session_button"
      @click="handleRefreshSession"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import AuthenticationButton from "@/components/general/AuthenticationButton.vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import PrimeButton from "primevue/button";
import { isRefreshTokenExpiryTimestampInSharedStoreReached, tryToRefreshSession } from "@/utils/SessionTimeoutUtils";
import Keycloak from "keycloak-js";
import { TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS } from "@/utils/Constants";
import { useSharedSessionStateStore } from "@/stores/stores";

export default defineComponent({
  inject: ["dialogRef"], // TODO try if you can inject keycloak Promise instead of passing it via the dialogRef
  name: "SessionTimeoutModal",
  components: { AuthenticationButton, PrimeButton },

  data() {
    return {
      displayedHeader: undefined as undefined | string,
      displayedText: undefined as undefined | string,
      showLogInButton: false,
      showRefreshButton: false,
      refreshButtonLabel: "Refresh Session",
      isTrackingOfRefreshTokenExpiryEnabled: false,
      hasExternalLogoutOccurred: false,
      keycloak: undefined as undefined | Keycloak,
      functionIdOfExpiryCheck: undefined as undefined | number,
      buttonClass: "p-button-sm uppercase d-letters w-15rem",
    };
  },

  watch: {
    currentRefreshTokenInSharedStore() {
      this.closeTheDialog();
      if (this.hasExternalLogoutOccurred) {
        void this.$router.push({ path: "/companies", replace: true });
      }
    },
  },

  computed: {
    currentRefreshTokenInSharedStore(): string | undefined {
      const currentRefreshTokenInSharedStore = useSharedSessionStateStore().refreshToken;
      if (currentRefreshTokenInSharedStore) {
        return currentRefreshTokenInSharedStore;
      } else {
        return undefined;
      }
    },
  },

  mounted() {
    this.getDataFromParentAndSet();
    if (this.isTrackingOfRefreshTokenExpiryEnabled) {
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
     * Handles a click on the refresh button.
     */
    handleRefreshSession() {
      tryToRefreshSession(this.keycloak as Keycloak);
      this.closeTheDialog();
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
        displayedHeader: string;
        displayedText: string;
        showLogInButton: boolean;
        showRefreshButton: boolean;
        isTrackingOfRefreshTokenExpiryEnabled: boolean;
        hasExternalLogoutOccurred: boolean;
        resolvedKeycloakPromise: Keycloak;
      };
      this.displayedHeader = dialogRefData.displayedHeader;
      this.displayedText = dialogRefData.displayedText;
      this.showLogInButton = dialogRefData.showLogInButton;
      this.showRefreshButton = dialogRefData.showRefreshButton;
      this.isTrackingOfRefreshTokenExpiryEnabled = dialogRefData.isTrackingOfRefreshTokenExpiryEnabled;
      this.hasExternalLogoutOccurred = dialogRefData.hasExternalLogoutOccurred;
      this.keycloak = dialogRefData.resolvedKeycloakPromise;
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
          this.displayedHeader = "Session closed";
          this.displayedText = "Your session has been closed due to inactivity. Login to start a new session.";
          this.refreshButtonLabel = "Login";
          clearInterval(this.functionIdOfExpiryCheck);
        }
      }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
    },
  },
});
</script>
