<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section">
      <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER" :company-id="companyID" :data-type="frameworkType">
        <MarginWrapper class="mb-2 bg-white">
          <BackButton id="backButton" class="mt-2" />
          <CompanyInformation :companyId="companyID" />
        </MarginWrapper>
        <component :is="frameworkToUploadComponent" :companyID="companyID" @datasetCreated="redirectToMyDatasets()" />
      </AuthorizationWrapper>
    </TheContent>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script setup lang="ts">
import TheHeader from '@/components/generics/TheHeader.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { DataTypeEnum } from '@clients/backend';

import CreateEsgDatenkatalogDataset from '@/components/forms/CreateEsgDatenkatalogDataset.vue';
import CreateSfdrDataset from '@/components/forms/CreateSfdrDataset.vue';
import CreateP2pDataset from '@/components/forms/CreateP2pDataset.vue';
import CreateEuTaxonomyForFinancials from '@/components/forms/CreateEuTaxonomyFinancials.vue';
import CreateEuTaxonomyNonFinancials from '@/components/forms/CreateEuTaxonomyNonFinancials.vue';
import CreateHeimathafenDataset from '@/components/forms/CreateHeimathafenDataset.vue';
import CreateLksgDataset from '@/components/forms/CreateLksgDataset.vue';
import CreateVsmeDataset from '@/components/forms/CreateVsmeDataset.vue';
import CreateAdditionalCompanyInformationDataset from '@/components/forms/CreateAdditionalCompanyInformationDataset.vue';
import CompanyInformation from '@/components/pages/CompanyInformation.vue';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import BackButton from '@/components/general/BackButton.vue';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import { redirectToMyDatasets } from '@/components/resources/uploadDataset/DatasetCreationRedirect';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakUtils';
import { computed } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import MarginWrapper from '@/components/wrapper/MarginWrapper.vue';
import CreateNuclearAndGasDataset from '@/components/forms/CreateNuclearAndGasDataset.vue';

const props = defineProps<{
  companyID: string;
  frameworkType: string;
}>();

const content: Content = contentData;
const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
const footerContent = footerPage?.sections;

const frameworkToUploadComponent = computed(() => {
  switch (props.frameworkType) {
    case `${DataTypeEnum.EutaxonomyNonFinancials}`:
      return CreateEuTaxonomyNonFinancials;
    case `${DataTypeEnum.EutaxonomyFinancials}`:
      return CreateEuTaxonomyForFinancials;
    case `${DataTypeEnum.P2p}`:
      return CreateP2pDataset;
    case `${DataTypeEnum.Lksg}`:
      return CreateLksgDataset;
    case `${DataTypeEnum.Sfdr}`:
      return CreateSfdrDataset;
    case `${DataTypeEnum.Heimathafen}`:
      return CreateHeimathafenDataset;
    case `${DataTypeEnum.EsgDatenkatalog}`:
      return CreateEsgDatenkatalogDataset;
    case `${DataTypeEnum.Vsme}`:
      return CreateVsmeDataset;
    case `${DataTypeEnum.NuclearAndGas}`:
      return CreateNuclearAndGasDataset;
    case `${DataTypeEnum.AdditionalCompanyInformation}`:
      return CreateAdditionalCompanyInformationDataset;
    default:
      return null;
  }
});
</script>
