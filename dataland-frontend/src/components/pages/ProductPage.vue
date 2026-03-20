<template>
  <main id="main-content" role="main">
    <ProductIntro />
    <ProductHowItWorks />
    <ProductGettingData />
    <div id="frameworks" aria-hidden="true"></div>
    <ProductFeatures />
    <ProductUseCases />
    <ProductCustomerStories />
    <ProductMembershipPricing />
    <ProductDocumentation />
    <ContactInquiryModal />
  </main>
</template>

<script setup lang="ts">
import ProductIntro from '@/components/resources/productPage/ProductIntro.vue';
import ProductHowItWorks from '@/components/resources/productPage/ProductHowItWorks.vue';
import ProductGettingData from '@/components/resources/productPage/ProductGettingData.vue';
import ProductFeatures from '@/components/resources/productPage/ProductFeatures.vue';
import ProductUseCases from '@/components/resources/productPage/ProductUseCases.vue';
import ProductCustomerStories from '@/components/resources/productPage/ProductCustomerStories.vue';
import ProductMembershipPricing from '@/components/resources/productPage/ProductMembershipPricing.vue';
import ProductDocumentation from '@/components/resources/productPage/ProductDocumentation.vue';
import ContactInquiryModal from '@/components/generics/ContactInquiryModal.vue';
</script>

<style scoped lang="scss">
main {
  margin-top: 132px;
  @media only screen and (max-width: $bp-md) {
    margin-top: 80px;
  }
}
</style>
