<template>
  <TheHeader/>
  <TheContent>
  <MarginWrapper class="text-left">
    <BackButton/>
  </MarginWrapper>
  <EuTaxoSearchBar/>
  <TaxonomySample :companyID="companyID"/>
  </TheContent>
</template>

<script>
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import TaxonomySample from "@/components/resources/taxonomy/TaxonomySample";

export default {
  name: "CompanyTaxonomy",
  components: {
    TaxonomySample,
    TheContent,
    TheHeader, BackButton, MarginWrapper, EuTaxoSearchBar},
  props: {
    companyID: {
      type: String
    }
  }
}
</script>