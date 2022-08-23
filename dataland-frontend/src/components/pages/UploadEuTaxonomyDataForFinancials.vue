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
        <CreateEUTaxonomyForFinancials :companyID="companyID" />
      </MarginWrapper>
    </TheContent>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script>
import TheHeader from "@/components/generics/TheHeader";
import TheContent from "@/components/generics/TheContent";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";
import CreateEUTaxonomyForFinancials from "@/components/forms/CreateEUTaxonomyForFinancials";
import CompanyInformation from "@/components/pages/CompanyInformation";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import DatalandFooter from "@/components/general/DatalandFooter";

export default {
  name: "UploadEuTaxonomyDataForFinancials",
  components: {
    DatalandFooter,
    CreateEUTaxonomyForFinancials,
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
