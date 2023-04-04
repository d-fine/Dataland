<template>
  <UserAuthenticationButtons v-if="showLogInButton" />
  {{ displayedText }}
  <PrimeButton
    v-if="showRefreshButton"
    :label="refreshButtonLabel"
    class="p-button-sm uppercase d-letters text-primary bg-white-alpha-10 w-15rem"
    name="refresh_session_button"
    @click="handleRefreshSession"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UserAuthenticationButtons from "@/components/general/UserAuthenticationButtons.vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import PrimeButton from "primevue/button";
import { isRefreshTokenExpiryTimestampInSharedStoreReached, tryToRefreshSession } from "@/utils/SessionTimeoutUtils";
import Keycloak from "keycloak-js";
import { TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS } from "@/utils/Constants";
import { useSharedSessionStateStore } from "@/stores/stores";

export default defineComponent({
  inject: ["dialogRef"],
  name: "SessionTimeoutModal",
  components: { UserAuthenticationButtons, PrimeButton },

  data() {
    return {
      displayedText: undefined as undefined | string,
      showLogInButton: false,
      showRefreshButton: false,
      refreshButtonLabel: "Refresh Session",
      isTrackingOfRefreshTokenExpiryEnabled: false,
      keycloak: undefined as undefined | Keycloak,
      functionIdOfExpiryCheck: undefined as undefined | number,
    };
  },

  watch: {
    currentRefreshTokenInSharedStore() {
      this.closeTheDialog();
    },
  },

  computed: {
    currentRefreshTokenInSharedStore(): string | undefined {
      const currentRefreshTokenInSharedStore = useSharedSessionStateStore().refreshToken;
      if (currentRefreshTokenInSharedStore) {
        return currentRefreshTokenInSharedStore; // TODO you could inject the current refresh token from App.vue
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
        displayedText: string;
        showLogInButton: boolean;
        showRefreshButton: boolean;
        isTrackingOfRefreshTokenExpiryEnabled: boolean;
        resolvedKeycloakPromise: Keycloak;
      };
      this.displayedText = dialogRefData.displayedText;
      this.showLogInButton = dialogRefData.showLogInButton;
      this.showRefreshButton = dialogRefData.showRefreshButton;
      this.isTrackingOfRefreshTokenExpiryEnabled = dialogRefData.isTrackingOfRefreshTokenExpiryEnabled;
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
          this.displayedText = "Your session was closed due to inactivity. Login to start a new session.";
          this.refreshButtonLabel = "Login";
          clearInterval(this.functionIdOfExpiryCheck);
        }
      }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
    },
  },
});
</script>
