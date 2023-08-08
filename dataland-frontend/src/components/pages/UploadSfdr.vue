<template>
  <AuthenticationWrapper>
    <TheHeader />
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER">
      <BackButton id="backButton" class="mt-2 pl-3" />
      <CompanyInformation :companyID="companyID" />
      <CreateSFDRDataset :companyID="companyID" @datasetCreated="redirectToMyDatasets(this.$router)" />
    </AuthorizationWrapper>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CreateSFDRDataset from "@/components/forms/CreateSfdrDataset.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import { redirectToMyDatasets } from "@/components/resources/uploadDataset/DatasetCreationRedirect";
import { KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";
import { defineComponent } from "vue";

export default defineComponent({
  name: "UploadSFDR",
  components: {
    AuthorizationWrapper,
    CreateSFDRDataset,
    TheHeader,
    AuthenticationWrapper,
    CompanyInformation,
    TheFooter,
    BackButton,
  },
  data() {
    return {
      KEYCLOAK_ROLE_UPLOADER,
    };
  },
  props: {
    companyID: {
      type: String,
    },
  },
  methods: { redirectToMyDatasets },
});
</script>
