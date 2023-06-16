<template>
  <AuthenticationWrapper>
    <TheHeader />
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER">
      <BackButton id="backButton" class="mt-2 pl-3" />
      <CompanyInformation :companyID="companyID" />
      <CreateEuTaxonomyForFinancials :companyID="companyID" @datasetCreated="redirectToMyDatasets(this.$router)" />
    </AuthorizationWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEuTaxonomyForFinancials from "@/components/forms/CreateEuTaxonomyForFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import { redirectToMyDatasets } from "@/components/resources/uploadDataset/DatasetCreationRedirect";
import { defineComponent } from "vue";
import { KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";

export default defineComponent({
  name: "UploadEuTaxonomyDataForFinancials",
  components: {
    AuthorizationWrapper,
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
      KEYCLOAK_ROLE_UPLOADER,
    };
  },
});
</script>
