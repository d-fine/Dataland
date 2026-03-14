<template>
  <main id="main-content" role="main">
    <TheAboutHero />
    <TheAboutTrustPillars />
    <TheAboutTeam />
    <TheAboutPrinciples />
    <TheAboutAdvisoryBoard />
    <TheAboutEcosystem />
    <TheAboutBottomCTA />
  </main>
</template>

<script setup lang="ts">
import TheAboutHero from '@/components/resources/aboutPage/TheAboutHero.vue';
import TheAboutTrustPillars from '@/components/resources/aboutPage/TheAboutTrustPillars.vue';
import TheAboutTeam from '@/components/resources/aboutPage/TheAboutTeam.vue';
import TheAboutPrinciples from '@/components/resources/aboutPage/TheAboutPrinciples.vue';
import TheAboutAdvisoryBoard from '@/components/resources/aboutPage/TheAboutAdvisoryBoard.vue';
import TheAboutEcosystem from '@/components/resources/aboutPage/TheAboutEcosystem.vue';
import TheAboutBottomCTA from '@/components/resources/aboutPage/TheAboutBottomCTA.vue';
</script>

<style lang="scss" scoped>
main {
  margin-top: 132px;
  @media only screen and (max-width: $bp-md) {
    margin-top: 80px;
  }
}
</style>
