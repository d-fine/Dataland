<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <MarginWrapper class="text-left">
        <BackButton />
      </MarginWrapper>
      <EuTaxoSearchBar />
      <TaxonomySample :companyID="companyID" />
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import TaxonomySample from "@/components/resources/taxonomy/TaxonomySample";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";

export default {
  name: "CompanyTaxonomy",
  components: {
    TaxonomySample,
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
    EuTaxoSearchBar,
    AuthenticationWrapper,
  },
  props: {
    companyID: {
      type: String,
    },
  },
};
</script>
