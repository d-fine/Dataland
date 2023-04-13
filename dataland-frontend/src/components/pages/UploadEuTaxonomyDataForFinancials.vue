<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <TheContent>
        <BackButton id="backButton" />
        <CompanyInformation :companyID="companyID" />
        <CreateEUTaxonomyForFinancials :companyID="companyID" @datasetCreated="handleDatasetCreated" />
      </TheContent>
    </UploaderRoleWrapper>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEUTaxonomyForFinancials from "@/components/forms/CreateEUTaxonomyForFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";
import { TIME_DELAY_BETWEEN_UPLOAD_AND_REDIRECT_IN_MS } from "@/utils/Constants";
import { defineComponent } from "vue";

export default defineComponent({
  name: "UploadEuTaxonomyDataForFinancials",
  components: {
    UploaderRoleWrapper,
    CreateEUTaxonomyForFinancials,
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
  methods: {
    /**
     * Executes a router push to the myDatasets page
     */
    handleDatasetCreated(): void {
      setTimeout(() => {
        void this.$router.push(`datasets`);
      }, TIME_DELAY_BETWEEN_UPLOAD_AND_REDIRECT_IN_MS);
    },
  },
});
</script>
