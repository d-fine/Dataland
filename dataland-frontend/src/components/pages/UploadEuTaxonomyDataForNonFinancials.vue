<template>
  <AuthenticationWrapper>
    <TheHeader />
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER">
      <BackButton id="backButton" class="mt-2 pl-3" />
      <CompanyInformation :companyID="companyID" />
      <CreateEuTaxonomyForNonFinancials :companyID="companyID" @datasetCreated="redirectToMyDatasets(this.$router)" />
    </AuthorizationWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateEuTaxonomyForNonFinancials from "@/components/forms/CreateEuTaxonomyForNonFinancials.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import { redirectToMyDatasets } from "@/components/resources/uploadDataset/DatasetCreationRedirect";
import { defineComponent } from "vue";
import { KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";

export default defineComponent({
  name: "UploadEutaxonomyDataForNonFinancials",
  components: {
    BackButton,
    AuthorizationWrapper,
    CreateEuTaxonomyForNonFinancials,
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
  data() {
    return {
      redirectToMyDatasets,
      KEYCLOAK_ROLE_UPLOADER,
    };
  },
});
</script>
