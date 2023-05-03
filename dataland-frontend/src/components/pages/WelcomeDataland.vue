<template>
  <LandingLogin v-if="!isMobile" />
  <LandingLoginMobile v-if="isMobile" />
  <SampleSection v-if="!isMobile" />
  <MarketingSection :isMobile="isMobile" />
  <TheFooter :isMobile="isMobile" />
</template>

<script lang="ts">
import LandingLogin from "@/components/resources/landing/LandingLogin.vue";
import LandingLoginMobile from "@/components/resources/landing/LandingLoginMobile.vue";
import MarketingSection from "@/components/resources/landing/MarketingSection.vue";
import SampleSection from "@/components/resources/landing/SampleSection.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import { defineComponent, inject } from "vue";
import { NavigationFailure, useRoute } from "vue-router";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import SessionDialog from "@/components/general/SessionDialog.vue";
import { useSharedSessionStateStore } from "@/stores/stores";
import { SessionDialogMode } from "@/utils/SessionTimeoutUtils";

export default defineComponent({
  name: "WelcomeDataland",
  components: { SampleSection, MarketingSection, LandingLogin, LandingLoginMobile, TheFooter },
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
    if (useRoute().query.externalLogout === "true") {
      this.openLogoutModal();
    }
    void this.checkAuthenticatedAndRedirectIfLoggedIn();
  },
  watch: {
    authenticated() {
      void this.checkAuthenticatedAndRedirectIfLoggedIn();
    },
    currentRefreshTokenInSharedStore() {
      void this.$router.push({ path: "/companies", replace: true });
    },
  },
  computed: {
    currentRefreshTokenInSharedStore() {
      return useSharedSessionStateStore().refreshToken;
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
     * Opens a pop-up to show the user that she/he was logged out.
     */
    openLogoutModal(): void {
      this.$dialog.open(SessionDialog, {
        props: {
          modal: true,
          dismissableMask: true,
          showHeader: false,
        },
        data: {
          sessionDialogMode: SessionDialogMode.ExternalLogout,
        },
        onClose: () => {
          void this.$router.replace("");
        },
      });
    },
  },
});
</script>
