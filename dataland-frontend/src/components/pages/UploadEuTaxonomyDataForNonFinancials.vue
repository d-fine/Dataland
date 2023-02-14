<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <TheContent>
        <MarginWrapper>
          <div class="grid align-items-end">
            <div class="col-9">
              <CompanyInformation :companyID="companyID" />
            </div>
          </div>
        </MarginWrapper>
        <MarginWrapper bg-class="paper-section" class="text-left">
          <CreateEUTaxonomyForNonFinancials :companyID="companyID" />
        </MarginWrapper>
      </TheContent>
    </UploaderRoleWrapper>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEUTaxonomyForNonFinancials from "@/components/forms/CreateEUTaxonomyForNonFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";

export default {
  name: "UploadEutaxonomyDataForNonFinancials",
  components: {
    UploaderRoleWrapper,
    CreateEUTaxonomyForNonFinancials,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    CompanyInformation,
    MarginWrapper,
    DatalandFooter,
  },
  props: {
    companyID: {
      type: String,
    },
  },
};
</script>
