<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <TheContent>
        <BackButton id="backButton" />
        <CompanyInformation :companyID="companyID" />
        <CreateEuTaxonomyForFinancials :companyID="companyID" @datasetCreated="redirectToMyDatasets(this.$router)" />
      </TheContent>
    </UploaderRoleWrapper>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEuTaxonomyForFinancials from "@/components/forms/CreateEuTaxonomyForFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";
import { redirectToMyDatasets } from "@/components/resources/uploadDataset/DatasetCreationRedirect";
import { defineComponent } from "vue";

export default defineComponent({
  name: "UploadEuTaxonomyDataForFinancials",
  components: {
    UploaderRoleWrapper,
    CreateEuTaxonomyForFinancials,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    CompanyInformation,
    BackButton,
    DatalandFooter,
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
