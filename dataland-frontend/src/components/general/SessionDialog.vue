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
  <PrimeButton
    label="FOR DEBUGGING: refreshTokens"
    class="p-button-sm uppercase d-letters text-primary bg-white-alpha-10 w-15rem"
    name="refresh_session_button"
    @click="refreshTokensDebuggingMethod"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UserAuthenticationButtons from "@/components/general/UserAuthenticationButtons.vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import PrimeButton from "primevue/button";
import {
  isCurrentRefreshTokenExpired,
  startSessionSetIntervalFunction, tryToRefreshSession,
  updateTokenAndItsExpiryTimestampAndStoreBoth,
} from "@/utils/SessionTimeoutUtils";
import Keycloak from "keycloak-js";
import { TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS } from "@/utils/Constants";
import { useFunctionIdsStore, useSessionStateStore } from "@/stores/stores";

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
    currentRefreshTokenInStore(newValue) {
      console.log("session dialog is open and noticed new refresh token => closing dialog");
      this.closeTheDialog();
    },
  },

  computed: {
    currentRefreshTokenInStore() {
      const currentRefreshTokenInStore = useSessionStateStore().refreshToken;
      if (currentRefreshTokenInStore) return currentRefreshTokenInStore; // you could inject the current refresh token from App.vue TODO
    },
  },

  mounted() {
    this.getDataFromParentAndSet();
    console.log("mounting the pop up"); // TODO debugging
    if (this.isTrackingOfRefreshTokenExpiryEnabled) {
      this.setIntervalForRefreshTokenExpiryCheck();
    }
  },

  beforeUnmount() {
    console.log("pop will be unmounted now"); // TODO debugging
    if (this.functionIdOfExpiryCheck) {
      console.log("clearing the expiry check setInterval"); // TODO debugging
      clearInterval(this.functionIdOfExpiryCheck);
    }
  },

  methods: {
    handleRefreshSession() {
      tryToRefreshSession(this.keycloak as Keycloak)
      this.closeTheDialog()
    },

    refreshTokensDebuggingMethod() {
      // TODO dummy function only for debugging
      if (this.keycloak) {
        updateTokenAndItsExpiryTimestampAndStoreBoth(this.keycloak);
        console.log("update token in method to " + this.keycloak.refreshToken);
      }
    },

    /**
     * Closes the dialog.
     */
    closeTheDialog() {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      dialogRefToDisplay.close();
    },

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

    setIntervalForRefreshTokenExpiryCheck() {
      console.log("starting setinveral for refresh token check"); // TODO debugging
      this.functionIdOfExpiryCheck = setInterval(() => {
        console.log("constantly checking if refresh token is expired"); // TODO debugging
        if (isCurrentRefreshTokenExpired()) {
          console.log("refresh token is expired => stopping this setInterval and changing texts on pop-up"); // TODO debugging
          this.displayedText = "Your session was closed due to inactivity. Login to start a new session.";
          this.refreshButtonLabel = "Login";
          clearInterval(this.functionIdOfExpiryCheck);
        }
      }, TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS);
    },
  },
});
</script>
