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
      <MarginWrapper bg-class="paper-section" class="text-left">
        <CreateEUTaxonomyForFinancials :companyID="companyID" />
      </MarginWrapper>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEUTaxonomyForFinancials from "@/components/forms/CreateEUTaxonomyForFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";

export default {
  name: "UploadEuTaxonomyDataForFinancials",
  components: {
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
