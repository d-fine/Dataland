<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section">
      <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER">
        <MarginWrapper class="mb-2 bg-white">
          <BackButton id="backButton" class="mt-2" />
          <CompanyInformation :companyId="companyID" />
        </MarginWrapper>
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

import CreateGdvDataset from "@/components/forms/CreateGdvDataset.vue";
import CreateLksgDataset from "@/components/forms/CreateLksgDataset.vue";
import CreateSfdrDataset from "@/components/forms/CreateSfdrDataset.vue";
import CreateP2pDataset from "@/components/forms/CreateP2pDataset.vue";
import CreateEuTaxonomyForNonFinancials from "@/components/forms/CreateEuTaxonomyForNonFinancials.vue";
import CreateEuTaxonomyForFinancials from "@/components/forms/CreateEuTaxonomyForFinancials.vue";

import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
import BackButton from "@/components/general/BackButton.vue";
import AuthorizationWrapper from "@/components/wrapper/AuthorizationWrapper.vue";
import { redirectToMyDatasets } from "@/components/resources/uploadDataset/DatasetCreationRedirect";
import { KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";
import { defineComponent } from "vue";
import TheContent from "@/components/generics/TheContent.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";

export default defineComponent({
  name: "UploadFormWrapper",
  components: {
    MarginWrapper,
    TheContent,
    AuthorizationWrapper,
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
        case `${DataTypeEnum.EutaxonomyNonFinancials}`:
          return CreateEuTaxonomyForNonFinancials;
        case `${DataTypeEnum.EutaxonomyFinancials}`:
          return CreateEuTaxonomyForFinancials;
        case `${DataTypeEnum.Lksg}`:
          return CreateLksgDataset;
        case `${DataTypeEnum.P2p}`:
          return CreateP2pDataset;
        case `${DataTypeEnum.Sfdr}`:
          return CreateSfdrDataset;
        case `${DataTypeEnum.Gdv}`:
          return CreateGdvDataset;
        default:
          return null;
      }
    },
  },
});
</script>
