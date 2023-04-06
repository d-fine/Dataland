<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <TheContent>
        <BackButton id="backButton" />
        <CompanyInformation :companyID="companyID" />
        <CreateEUTaxonomyForFinancials :companyID="companyID" />
      </TheContent>
    </UploaderRoleWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEUTaxonomyForFinancials from "@/components/forms/CreateEUTaxonomyForFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";

export default {
  name: "UploadEuTaxonomyDataForFinancials",
  components: {
    UploaderRoleWrapper,
    CreateEUTaxonomyForFinancials,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    CompanyInformation,
    BackButton,
    TheFooter,
  },
  props: {
    companyID: {
      type: String,
    },
  },
};
</script>
