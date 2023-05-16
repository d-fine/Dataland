<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <BackButton id="backButton" class="mt-2 pl-3" />
      <CompanyInformation :companyID="companyID" />
      <CreateEuTaxonomyForFinancials :companyID="companyID" @datasetCreated="redirectToMyDatasets(this.$router)" />
    </UploaderRoleWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEuTaxonomyForFinancials from "@/components/forms/CreateEUTaxonomyForFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";
import { redirectToMyDatasets } from "@/components/resources/uploadDataset/DatasetCreationRedirect";
import { defineComponent } from "vue";

export default defineComponent({
  name: "UploadEuTaxonomyDataForFinancials",
  components: {
    UploaderRoleWrapper,
    CreateEuTaxonomyForFinancials,
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
  data() {
    return {
      redirectToMyDatasets,
    };
  },
});
</script>
