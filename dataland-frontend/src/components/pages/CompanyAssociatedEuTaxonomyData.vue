<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <MarginWrapper class="text-left mt-2">
        <BackButton />
        <EuTaxoSearchBar class="mt-2" v-model="currentInput" @companies-received="handleQueryCompany" />
      </MarginWrapper>
      <MarginWrapper>
        <div class="grid align-items-end">
          <div class="col-9">
            <CompanyInformation :companyID="companyID" />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper bgClass="surface-800">
        <TaxonomyData :companyID="companyID" />
      </MarginWrapper>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";
import TheHeader from "@/components/structure/TheHeader";
import TheContent from "@/components/structure/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CompanyInformation from "@/components/pages/CompanyInformation";
import TaxonomyData from "@/components/resources/taxonomy/TaxonomyData";

export default {
  name: "CompanyAssociatedEuTaxonomyData",
  components: {
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
    EuTaxoSearchBar,
    AuthenticationWrapper,
    CompanyInformation,
    TaxonomyData,
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
      this.$router.push({ name: "Search Companies for Framework Data", query: { input: this.currentInput } });
    },
  },
};
</script>
