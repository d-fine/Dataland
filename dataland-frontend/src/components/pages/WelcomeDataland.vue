<template>
  <LandingLogin :isMobile="isMobile" />
  <SampleSection v-if="!isMobile" />
  <MarketingSection :isMobile="isMobile" />
  <DatalandFooter :isMobile="isMobile" />
</template>

<script lang="ts">
import LandingLogin from "@/components/resources/landing/LandingLogin.vue";
import MarketingSection from "@/components/resources/landing/MarketingSection.vue";
import SampleSection from "@/components/resources/landing/SampleSection.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { defineComponent, inject } from "vue";
import { NavigationFailure } from "vue-router";

export default defineComponent({
  name: "WelcomeDataland",
  components: { SampleSection, MarketingSection, LandingLogin, DatalandFooter },
  setup() {
    return {
      authenticated: inject<boolean>("authenticated"),
    };
  },
  props: {
    isMobile: {
      type: Boolean,
    },
  },
  mounted() {
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
    checkAuthenticatedAndRedirectIfLoggedIn(): Promise<NavigationFailure | void | undefined> {
      if (this.authenticated === true) {
        return this.$router.push({ path: "/companies", replace: true });
      }
      return Promise.resolve();
    },
  },
});
</script>
