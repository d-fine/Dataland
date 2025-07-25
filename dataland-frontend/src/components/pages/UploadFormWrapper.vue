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
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script setup lang="ts">
import CreateEuTaxonomyForFinancials from '@/components/forms/CreateEuTaxonomyFinancials.vue';
import CreateEuTaxonomyNonFinancials from '@/components/forms/CreateEuTaxonomyNonFinancials.vue';
import CreateLksgDataset from '@/components/forms/CreateLksgDataset.vue';
import CreateNuclearAndGasDataset from '@/components/forms/CreateNuclearAndGasDataset.vue';
import CreateSfdrDataset from '@/components/forms/CreateSfdrDataset.vue';
import CreateVsmeDataset from '@/components/forms/CreateVsmeDataset.vue';
import BackButton from '@/components/general/BackButton.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import CompanyInformation from '@/components/pages/CompanyInformation.vue';
import { redirectToMyDatasets } from '@/components/resources/uploadDataset/DatasetCreationRedirect';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import MarginWrapper from '@/components/wrapper/MarginWrapper.vue';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { DataTypeEnum } from '@clients/backend';
import { computed } from 'vue';

const props = defineProps<{
  companyID: string;
  frameworkType: string;
}>();

const frameworkToUploadComponent = computed(() => {
  switch (props.frameworkType) {
    case `${DataTypeEnum.EutaxonomyNonFinancials}`:
      return CreateEuTaxonomyNonFinancials;
    case `${DataTypeEnum.EutaxonomyFinancials}`:
      return CreateEuTaxonomyForFinancials;
    case `${DataTypeEnum.Lksg}`:
      return CreateLksgDataset;
    case `${DataTypeEnum.Sfdr}`:
      return CreateSfdrDataset;
    case `${DataTypeEnum.Vsme}`:
      return CreateVsmeDataset;
    case `${DataTypeEnum.NuclearAndGas}`:
      return CreateNuclearAndGasDataset;
    default:
      return null;
  }
});
</script>
