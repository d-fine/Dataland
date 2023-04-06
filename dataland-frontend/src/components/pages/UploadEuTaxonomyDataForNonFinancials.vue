<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <BackButton id="backButton" class="mt-2 pl-3" />
      <CompanyInformation :companyID="companyID" />
      <CreateEUTaxonomyForNonFinancials :companyID="companyID" />
    </UploaderRoleWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEUTaxonomyForNonFinancials from "@/components/forms/CreateEUTaxonomyForNonFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";

export default {
  name: "UploadEutaxonomyDataForNonFinancials",
  components: {
    BackButton,
    UploaderRoleWrapper,
    CreateEUTaxonomyForNonFinancials,
    TheHeader,
    AuthenticationWrapper,
    CompanyInformation,
    TheFooter,
  },
  props: {
    companyID: {
      type: String,
    },
  },
};
</script>
