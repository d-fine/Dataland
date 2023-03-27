<template>
  <UserAuthenticationButtons v-if="showLogInButton" />
  <h1 v-text="sessionStateStore.token" />
  {{ displayedText }}
  <PrimeButton
    v-if="showRefreshButton"
    :label="refreshButtonLabel"
    class="p-button-sm uppercase d-letters text-primary bg-white-alpha-10 w-15rem"
    name="refresh_session_button"
    @click="handleClickOnRefreshSession"
  />
  <PrimeButton
    label="FOR DEBUGGIN: refreshTokens"
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
import { isCurrentRefreshTokenExpired } from "@/utils/SessionTimeoutUtils";
import Keycloak from "keycloak-js";
import { TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS } from "@/utils/Constants";
import { useSessionStateStore } from "@/stores/stores";

export default defineComponent({
  inject: ["dialogRef"],
  name: "SessionTimeoutModal",
  components: { UserAuthenticationButtons, PrimeButton },

  data() {
    return {
      sessionStateStore: useSessionStateStore(),
      displayedText: undefined as undefined | string,
      showLogInButton: false,
      showRefreshButton: false,
      refreshButtonLabel: "Refresh Session",
      isTrackingOfRefreshTokenExpiryEnabled: false,
      keycloak: undefined as undefined | Keycloak,
      functionIdOfExpiryCheck: undefined as undefined | number,
    };
  },

  mounted() {
    this.getDataFromParentAndSet();
    console.log("mounting the pop up"); // TODO debugging
    if (this.isTrackingOfRefreshTokenExpiryEnabled) {
      this.setIntervalForRefreshTokenExpiredCheck();
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
    refreshTokensDebuggingMethod() {
      // TODO dummy function only for debugging
      if (this.keycloak) {
        this.keycloak?.updateToken(-1);
        console.log("update token in method to " + this.keycloak.refreshToken);
        console.log("set manually now");
        const dummyNewString = (" " + this.keycloak.refreshToken).slice(1);
        this.keycloak.refreshToken = dummyNewString;
        console.log("new manually set refresh token " + this.keycloak.refreshToken);
      }
    },

    /**
     * Handles the click-event on the "refresh session" button.
     */
    handleClickOnRefreshSession() {
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

    setIntervalForRefreshTokenExpiredCheck() {
      console.log("starting setinveral for refresh token check"); // TODO debugging
      this.functionIdOfExpiryCheck = setInterval(() => {
        console.log("constantly checking if refresh token is expired"); // TODO debugging
        if (isCurrentRefreshTokenExpired(this.keycloak as Keycloak)) {
          useSessionStateStore().isRefreshTokenExpired = true;
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
