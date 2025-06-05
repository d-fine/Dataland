<template>
  <Listbox
      v-model="selectedPortfolios"
      :options="allUserPortfolios"
      multiple
      optionLabel="portfolioName"
  />
  <PrimeButton class="primary-button" aria-label="Add Company" @click="handleCompanyAddition">
    <span>Add company to portfolio(s)</span>
  </PrimeButton>
</template>

<script setup lang="ts">
import {defineEmits, inject, type PropType, type Ref, ref} from 'vue';
import {ApiClientProvider} from '@/services/ApiClients.ts';
import {assertDefined} from '@/utils/TypeScriptUtils.ts';
import Listbox from 'primevue/listbox';
import PrimeButton from 'primevue/button';
import type Keycloak from 'keycloak-js';
import type {DynamicDialogInstance} from "primevue/dynamicdialogoptions";

export interface ReducedBasePortfolio {
  portfolioId: string,
  portfolioName: string,
  companyIds: string[]
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const selectedPortfolios = ref<ReducedBasePortfolio[]>([]);

const props = defineProps({
  companyId: {
    type: String,
    required: true,
  },
  allUserPortfolios: {
    type: Array as PropType<Array<ReducedBasePortfolio>>,
    required: true,
  },
});

const emit = defineEmits(['closePortfolioModal']);

const handleCompanyAddition = (): void => {
  if (selectedPortfolios.value.length === 0) return;
  selectedPortfolios.value.forEach(async (selectedPortfolio) => {
    console.log("selectedPortfolio: " + JSON.stringify(selectedPortfolio) + "!!!!!!!");
    /*
    const reducedBasePortfolio = allUserPortfolios.find(
        (reducedBasePortfolio) => {
          console.log("reducedBasePortfolio.portfolioName: " + reducedBasePortfolio.portfolioName + "!!!!!!!")
          return reducedBasePortfolio.portfolioName === selectedPortfolio.portfolioName
        }
    );
     */
    await apiClientProvider.apiClients.portfolioController.replacePortfolio(
        selectedPortfolio.portfolioId,
        {
          portfolioName: selectedPortfolio.portfolioName,
          // as unknown as Set<string> cast required to ensure proper json is created
          companyIds: [...selectedPortfolio.companyIds, props.companyId] as unknown as Set<string>,
        });
  });
  closeDialog();
};

const closeDialog = (): void => {
  selectedPortfolios.value = [];
  dialogRef?.value.close();
};
</script>

<style scoped lang="scss"></style>
