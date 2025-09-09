<template>
  <div v-if="!displaySuccessMessage" class="container">
    <Listbox
      v-model="selectedPortfolios"
      :options="allUserPortfolios"
      optionLabel="portfolioName"
      multiple
      :disabled="isLoading"
      data-test="portfolioSelectionListbox"
      :pt="{
        option: {
          style: 'max-width: 100%; width: 100%, overflow: hidden; text-overflow: ellipsis; white-space: nowrap;',
        },
        list: {
          style: 'width: 100%;',
        },
      }"
      style="width: 100%; margin-bottom: 0.5rem"
    />

    <p class="dataland-info-text small">
      Choose one or more portfolios you wish to add this company to. Portfolios already containing the company will not
      be modified.
    </p>

    <Message v-if="errorMessage" severity="error" class="my-3">
      {{ errorMessage }}
    </Message>

    <PrimeButton
      :disabled="selectedPortfolios.length === 0 || isLoading"
      :loading="isLoading"
      @click="handleCompanyAddition"
      data-test="saveButton"
      icon="pi pi-plus"
      :label="'Add company to portfolio' + (selectedPortfolios.length > 1 ? 's' : '')"
    />
  </div>
  <Message v-else severity="success" size="large" class="container">Successfully added!</Message>
</template>

<script setup lang="ts">
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { type BasePortfolio } from '@clients/userservice';
import { AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import Message from 'primevue/message';
import Listbox from 'primevue/listbox';
import { inject, onMounted, type Ref, ref } from 'vue';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const data = dialogRef?.value.data;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

let companyId: string;

const allUserPortfolios = ref<BasePortfolio[]>([]);
const selectedPortfolios = ref<BasePortfolio[]>([]);

const errorMessage = ref('');
const isLoading = ref(false);
const displaySuccessMessage = ref(false);

onMounted(() => {
  if (!data?.companyId) return;
  companyId = data.companyId;
  allUserPortfolios.value = data.allUserPortfolios;
});

/**
 * Handles the addition of the company to the selected portfolio(s) and closes the dialog
 * unless there is an error, in which case the modal stays open and the error message is
 * displayed.
 */
async function handleCompanyAddition(): Promise<void> {
  if (selectedPortfolios.value.length === 0) return;

  isLoading.value = true;

  try {
    await Promise.all(
      selectedPortfolios.value.map((selectedPortfolio) => {
        const updatedCompanyIds = [...new Set([...selectedPortfolio.companyIds, companyId])];

        return apiClientProvider.apiClients.portfolioController.replacePortfolio(selectedPortfolio.portfolioId, {
          portfolioName: selectedPortfolio.portfolioName,
          // as unknown as Set<string> cast required to ensure proper json is created
          companyIds: updatedCompanyIds as unknown as Set<string>,
          isMonitored: selectedPortfolio.isMonitored,
          startingMonitoringPeriod: selectedPortfolio.startingMonitoringPeriod,
          monitoredFrameworks: selectedPortfolio.monitoredFrameworks,
        });
      })
    );

    closeDialog();
  } catch (error) {
    if (error instanceof AxiosError) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = 'Failed to add company to selected portfolios.';
    }
    console.error('Error adding company to portfolios:', error);
  } finally {
    isLoading.value = false;
  }
}

/**
 * Resets selectedPortfolios and errorMessage to their initial values, displays a success message
 * for 2 seconds, then closes the dialog.
 */
function closeDialog(): void {
  selectedPortfolios.value = [];
  errorMessage.value = '';
  displaySuccessMessage.value = true;
  setTimeout(() => {
    dialogRef?.value.close();
  }, 200000);
}
</script>

<style scoped lang="scss">
.container {
  width: 22rem;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  align-items: center;
  margin-top: var(--spacing-xs);
}

.gray-text {
  color: var(--gray);
}
</style>
