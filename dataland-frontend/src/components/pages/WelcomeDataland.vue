<template>
  <LandingLogin v-if="!isMobile" />
  <LandingLoginMobile v-if="isMobile" />
  <SampleSection v-if="!isMobile" />
  <MarketingSection :isMobile="isMobile" />
  <DatalandFooter :isMobile="isMobile" />
</template>

<script lang="ts">
import LandingLogin from "@/components/resources/landing/LandingLogin.vue";
import LandingLoginMobile from "@/components/resources/landing/LandingLoginMobile.vue";
import MarketingSection from "@/components/resources/landing/MarketingSection.vue";
import SampleSection from "@/components/resources/landing/SampleSection.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { defineComponent, inject } from "vue";
import { NavigationFailure } from "vue-router";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { useRoute } from "vue-router";
import SessionTimeoutModal from "@/components/general/SessionTimeoutModal.vue";

export default defineComponent({
  name: "WelcomeDataland",
  components: { SampleSection, MarketingSection, LandingLogin, LandingLoginMobile, DatalandFooter },
  setup() {
    return {
      authenticated: inject<boolean>("authenticated"),
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  props: {
    isMobile: {
      type: Boolean,
    },
  },
  mounted() {
    if (useRoute().query.sessionClosed === "true") {
      this.openLogoutModal("Your session could not be refreshed because it is closed. Please log in again.");
    }
    if (useRoute().query.externalLogout === "true") {
      this.openLogoutModal("You have been logged out. Do you want to login again?");
    }
    void this.checkAuthenticatedAndRedirectIfLoggedIn();
  },
  watch: {
    authenticated() {
      void this.checkAuthenticatedAndRedirectIfLoggedIn();
    },
  },
  methods: {
    /**
     * Redirects to the /companies page if the user is logged in. Does nothing otherwise
     *
     * @returns a promise of the redirect operation (or a resolved promise if no redirect needs to occur)
     */
    async checkAuthenticatedAndRedirectIfLoggedIn(): Promise<NavigationFailure | void | undefined> {
      const keycloak = await assertDefined(this.getKeycloakPromise)();
      if (keycloak.authenticated === true) {
        return this.$router.push({ path: "/companies", replace: true });
      }
      return Promise.resolve();
    },

    /**
     * Opens a pop-up to show the user that he was logged out. Since there can be multiple reasons for this, the
     * displayed text should be passed as param.
     *
     * @param headerText contains the text to display in the header
     */
    openLogoutModal(headerText: string): void {
      this.$dialog.open(SessionTimeoutModal, {
        props: {
          header: headerText,
          modal: true,
          dismissableMask: true,
        },
        data: {
          showLogInButton: true,
        },
        onClose: () => {
          void this.$router.replace("");
        },
      });
    },
  },
});
</script>
