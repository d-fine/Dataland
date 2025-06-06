<template>
  <Listbox
    v-model="selectedPortfolios"
    :options="allUserPortfolios"
    multiple
    :meta-key-selection="false"
    optionLabel="portfolioName"
  />
  <PrimeButton class="primary-button" aria-label="Add Company" @click="handleCompanyAddition">
    <span>Add company to portfolio(s)</span>
  </PrimeButton>
</template>

<script setup lang="ts">
import { inject, onMounted, type Ref, ref } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Listbox from 'primevue/listbox';
import PrimeButton from 'primevue/button';
import type Keycloak from 'keycloak-js';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';

export interface ReducedBasePortfolio {
  portfolioId: string;
  portfolioName: string;
  companyIds: string[];
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const data = dialogRef?.value.data;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

let companyId: string;

const allUserPortfolios = ref<ReducedBasePortfolio[]>([]);
const selectedPortfolios = ref<ReducedBasePortfolio[]>([]);

onMounted(() => {
  companyId = data.companyId;
  allUserPortfolios.value = data.allUserPortfolios;
});

const handleCompanyAddition = (): void => {
  if (selectedPortfolios.value.length === 0) return;
  selectedPortfolios.value.forEach((selectedPortfolio) => {
    void apiClientProvider.apiClients.portfolioController.replacePortfolio(selectedPortfolio.portfolioId, {
      portfolioName: selectedPortfolio.portfolioName,
      // as unknown as Set<string> cast required to ensure proper json is created
      companyIds: [...selectedPortfolio.companyIds, companyId] as unknown as Set<string>,
    });
  });
  closeDialog();
};

const closeDialog = (): void => {
  selectedPortfolios.value = [];
  dialogRef?.value.close();
};
</script>

<style scoped lang="scss">
.p-highlight {
  background-color: #f0f0f0; /* Change background color when highlighted */
  color: #333; /* Change text color */
}

.p-focus {
  border: 2px solid #000; /* Perhaps give a border to indicate focus */
  outline: none; /* Remove default outline */
}
</style>
