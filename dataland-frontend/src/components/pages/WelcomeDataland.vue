<template>
  <LandingLogin/>
  <SampleSection/>
  <MarketingSection/>
  <LandingFooter/>
</template>

<script>
import LandingLogin from "@/components/resources/landing/LandingLogin.vue"
import MarketingSection from "@/components/resources/landing/MarketingSection";
import LandingFooter from "@/components/resources/landing/LandingFooter";
import SampleSection from "@/components/resources/landing/SampleSection";
export default {
  name: 'WelcomeDataland',
  components: {SampleSection, LandingFooter, MarketingSection, LandingLogin}
}
</script>


