<template>
  <LandingLogin/>
  <SampleSection/>
  <MarketingSection/>
  <LandingFooter/>
</template>

<script>
import LandingLogin from "@/components/resources/landing/LandingLogin.vue"
import SampleSection from "@/components/resources/landing/MarketingSection";
import MarketingSection from "@/components/resources/landing/SampleSection";
import LandingFooter from "@/components/resources/landing/LandingFooter";
export default {
  name: 'WelcomeDataland',
  components: {LandingFooter, MarketingSection, SampleSection, LandingLogin}
}
</script>


