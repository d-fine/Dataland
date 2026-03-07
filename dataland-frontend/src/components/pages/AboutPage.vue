<template>
  <main role="main">
    <ContactInquiryModal />
    <TheAboutHero />
    <TheAboutTrustPillars />
    <TheAboutTeam />
    <TheAboutAdvisoryBoard />
    <TheAboutSponsors />
    <TheAboutPartners />
    <TheAboutBottomCTA />
  </main>
</template>

<script setup lang="ts">
import ContactInquiryModal from '@/components/general/ContactInquiryModal.vue';
import TheAboutHero from '@/components/resources/aboutPage/TheAboutHero.vue';
import TheAboutTrustPillars from '@/components/resources/aboutPage/TheAboutTrustPillars.vue';
import TheAboutTeam from '@/components/resources/aboutPage/TheAboutTeam.vue';
import TheAboutAdvisoryBoard from '@/components/resources/aboutPage/TheAboutAdvisoryBoard.vue';
import TheAboutSponsors from '@/components/resources/aboutPage/TheAboutSponsors.vue';
import TheAboutPartners from '@/components/resources/aboutPage/TheAboutPartners.vue';
import TheAboutBottomCTA from '@/components/resources/aboutPage/TheAboutBottomCTA.vue';
</script>

<style lang="scss" scoped>
main {
  margin-top: 132px;
  @media only screen and (max-width: 768px) {
    margin-top: 80px;
  }
}
</style>