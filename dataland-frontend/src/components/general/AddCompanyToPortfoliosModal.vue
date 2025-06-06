<template>
  <Listbox
      v-model="selectedPortfolios"
      :options="allUserPortfolios"
      multiple
      :meta-key-selection="false"
      optionLabel="portfolioName"
      :disabled="isLoading"
      data-test="'portfolioSelectionListbox'"
  />

  <Message v-if="errorMessage" severity="error" class="my-3">
    {{ errorMessage }}
  </Message>

  <PrimeButton
    class="primary-button"
    aria-label="Add Company"
    :disabled="selectedPortfolios.length === 0 || isLoading"
    :loading="isLoading"
    @click="handleCompanyAddition"
    :data-test="'saveButton'"
  >
    <span>Add company to portfolio(s)</span>
  </PrimeButton>
</template>

<script setup lang="ts">
import {defineEmits, inject, onMounted, type Ref, ref} from 'vue';
import {ApiClientProvider} from '@/services/ApiClients.ts';
import {assertDefined} from '@/utils/TypeScriptUtils.ts';
import Listbox from 'primevue/listbox';
import PrimeButton from 'primevue/button';
import Message from 'primevue/message';
import type Keycloak from 'keycloak-js';
import type {DynamicDialogInstance} from "primevue/dynamicdialogoptions";
import { AxiosError } from 'axios';

export interface ReducedBasePortfolio {
  portfolioId: string,
  portfolioName: string,
  companyIds: string[]
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const data = dialogRef?.value.data;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

let companyId: string;

const allUserPortfolios = ref<ReducedBasePortfolio[]>([]);
const selectedPortfolios = ref<ReducedBasePortfolio[]>([]);

const errorMessage = ref("");
const isLoading = ref(false)

const emit = defineEmits(['closePortfolioModal']);

onMounted(() => {
  companyId = data.companyId;
  allUserPortfolios.value = data.allUserPortfolios;
  errorMessage.value = '';
});

const handleCompanyAddition = async (): Promise<void> => {
  if (selectedPortfolios.value.length === 0) return;

  isLoading.value = true;
  errorMessage.value = '';

  try {
    await Promise.all(
      selectedPortfolios.value.map((selectedPortfolio) => {
      const updatedCompanyIds = [...new Set([...selectedPortfolio.companyIds, companyId])]

      return apiClientProvider.apiClients.portfolioController.replacePortfolio(
        selectedPortfolio.portfolioId,
        {
          portfolioName: selectedPortfolio.portfolioName,
          companyIds: updatedCompanyIds as unknown as Set<string>,
        }
      );
    })
  );

    closeDialog();
  } catch (error) {
    if (error instanceof AxiosError) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = "Failed to add company to selected portfolios."
    }
    console.error("Error adding company to portfolios:", error);
  } finally {
    isLoading.value = false;
  }
};

const closeDialog = (): void => {
  selectedPortfolios.value = [];
  errorMessage.value = "";
  dialogRef?.value.close();
};
</script>

<style scoped lang="scss"></style>
