<template>
  <LandingLogin />
  <SampleSection />
  <MarketingSection />
  <DatalandFooter />
</template>

<script>
import LandingLogin from "@/components/resources/landing/LandingLogin.vue";
import MarketingSection from "@/components/resources/landing/MarketingSection";
import DatalandFooter from "@/components/general/DatalandFooter";
import SampleSection from "@/components/resources/landing/SampleSection";
export default {
  name: "WelcomeDataland",
  components: { SampleSection, DatalandFooter, MarketingSection, LandingLogin },
  inject: ["authenticated"],

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
        this.$router.push({ path: "/companies", replace: true });
      }
    },
  },
};
</script>
