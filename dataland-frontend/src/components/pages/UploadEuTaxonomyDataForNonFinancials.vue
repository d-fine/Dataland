<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <MarginWrapper>
        <div class="grid align-items-end">
          <div class="col-9">
            <CompanyInformation :companyID="companyID" />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper bg-class="surface-800" class="text-left">
        <CreateEUTaxonomyForNonFinancials :companyID="companyID" />
      </MarginWrapper>
    </TheContent>
  </AuthenticationWrapper>
</template>
<script>
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CreateEUTaxonomyForNonFinancials from "@/components/forms/CreateEUTaxonomyForNonFinancials";
import CompanyInformation from "@/components/pages/CompanyInformation";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "UploadEutaxonomyDataForNonFinancials",
  components: {
    CreateEUTaxonomyForNonFinancials,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    CompanyInformation,
    MarginWrapper,
  },
  props: {
    companyID: {
      type: String,
    },
  },
};
</script>
