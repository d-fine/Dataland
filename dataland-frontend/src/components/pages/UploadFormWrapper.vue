<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER">
        <BackButton id="backButton" class="mt-2 pl-3" />
        <CompanyInformation :companyID="companyID" />
        <h1>{{ frameworkType }}</h1>
        <component
          :is="frameworkToUploadComponent"
          :companyID="companyID"
          @datasetCreated="redirectToMyDatasets(this.$router)"
        />
      </AuthorizationWrapper>
    </TheContent>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import { DataTypeEnum } from "@clients/backend";

import CreateLksgDataset from "@/components/forms/CreateLksgDataset.vue";
import CreateSfdrDataset from "@/components/forms/CreateSfdrDataset.vue";
import CreateP2pDataset from "@/components/forms/CreateP2pDataset.vue";
import CreateNewEuTaxonomyForNonFinancials from "@/components/forms/CreateNewEuTaxonomyForNonFinancials.vue";

import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/general/TheFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import { redirectToMyDatasets } from "@/components/resources/uploadDataset/DatasetCreationRedirect";
import { KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";
import { defineComponent } from "vue";
import TheContent from "@/components/generics/TheContent.vue";

export default defineComponent({
  name: "UploadFormWrapper",
  components: {
    TheContent,
    AuthorizationWrapper,
    CreateLksgDataset,
    CreateSfdrDataset,
    CreateP2pDataset,
    CreateNewEuTaxonomyForNonFinancials,
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
    companyID: String,
    frameworkType: String,
  },
  methods: { redirectToMyDatasets },
  computed: {
    frameworkToUploadComponent() {
      switch (this.frameworkType) {
        case "new-eutaxonomy-non-financials":
          return CreateNewEuTaxonomyForNonFinancials;
        case "lksg":
          return CreateLksgDataset;
          break;
        case "p2p":
          return CreateP2pDataset;
          break;
        case "sfdr":
          return CreateSfdrDataset;
          break;
        default:
          return null;
      }
    },
  },
});
</script>
