<template>
  <main id="main-content" role="main">
    <TheAboutCompany />
    <TheAboutTeam />
    <TheAboutPartners />
    <TheAboutUpdates />
    <TheAboutContact />
    <ContactInquiryModal />
  </main>
</template>

<script setup lang="ts">
import TheAboutCompany from '@/components/resources/aboutPage/TheAboutCompany.vue';
import TheAboutTeam from '@/components/resources/aboutPage/TheAboutTeam.vue';
import TheAboutPartners from '@/components/resources/aboutPage/TheAboutPartners.vue';
import TheAboutUpdates from '@/components/resources/aboutPage/TheAboutUpdates.vue';
import TheAboutContact from '@/components/resources/aboutPage/TheAboutContact.vue';
import ContactInquiryModal from '@/components/generics/ContactInquiryModal.vue';
</script>

<style lang="scss" scoped>
main {
  margin-top: 132px;
  @media only screen and (max-width: $bp-md) {
    margin-top: 80px;
  }
}
</style>
