<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TopTabNavigationMenu :tabs="['EU Taxonomy']" />
    <TheContent>
      <MarginWrapper class="text-left mt-2">
        <BackButton />
        <EuTaxoSearchBar class="mt-2" v-model="currentInput" @companies-received="handleQueryCompany" />
      </MarginWrapper>
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
import TopTabNavigationMenu from "@/components/menus/TopTabNavigationMenu";

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
    TopTabNavigationMenu,
  },
  data() {
    return {
      currentInput: "",
    };
  },
  props: {
    companyID: {
      type: String,
    },
  },
  methods: {
    handleQueryCompany() {
      this.$router.push({ name: "Search Eu Taxonomy", query: { input: this.currentInput } });
    },
  },
};
</script>
