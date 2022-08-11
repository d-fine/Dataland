<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <MarginWrapper class="text-left mt-2">
        <BackButton />
        <FrameworkDataSearchBar class="mt-2" v-model="currentInput" @companies-received="handleQueryCompany" />
      </MarginWrapper>
      <MarginWrapper>
        <div class="grid align-items-end">
          <div class="col-9">
            <CompanyInformation :companyID="companyID" />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper bgClass="surface-800">
        <EuTaxonomyData :companyID="companyID" />
      </MarginWrapper>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script>
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import BackButton from "@/components/general/BackButton";
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CompanyInformation from "@/components/pages/CompanyInformation";
import EuTaxonomyData from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyData";

export default {
  name: "CompanyAssociatedEuTaxonomyData",
  components: {
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
    FrameworkDataSearchBar,
    AuthenticationWrapper,
    CompanyInformation,
    EuTaxonomyData,
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
