<template>
  <LandingLogin/>
  <MarketingSection/>
  <LandingFooter/>
</template>

<script>
import LandingLogin from "@/components/resources/landing/LandingLogin.vue"
import MarketingSection from "@/components/resources/landing/MarketingSection";
import LandingFooter from "@/components/resources/landing/LandingFooter";
export default {
  name: 'WelcomeDataland',
  components: {LandingFooter, MarketingSection, LandingLogin}
}
</script>


