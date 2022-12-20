<template>
  <LandingLogin />
  <SampleSection />
  <MarketingSection />
</template>

<script lang="ts">
import LandingLogin from "@/components/resources/landing/LandingLogin.vue";
import MarketingSection from "@/components/resources/landing/MarketingSection.vue";
import SampleSection from "@/components/resources/landing/SampleSection.vue";
import { defineComponent, inject } from "vue";

export default defineComponent({
  name: "WelcomeDataland",
  components: { SampleSection, MarketingSection, LandingLogin },
  setup() {
    return {
      authenticated: inject<boolean>("authenticated"),
    };
  },

  mounted() {
    this.checkAuthenticatedAndRedirectIfLoggedIn();
  },
  watch: {
    authenticated() {
      this.checkAuthenticatedAndRedirectIfLoggedIn();
    },
  },
  methods: {
    checkAuthenticatedAndRedirectIfLoggedIn() {
      if (this.authenticated === true) {
        return this.$router.push({ path: "/companies", replace: true });
      }
    },
  },
});
</script>
