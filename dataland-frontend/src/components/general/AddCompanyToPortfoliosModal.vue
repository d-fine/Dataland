<template>
  <Listbox
    v-model="selectedPortfolios"
    :options="allUserPortfolios"
    :virtual-scroller-options="{ itemSize: 30, scrollWidth: '100%', scrollHeight: '300px', autoSize: true }"
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
    <span>Add company to portfolio</span>
  </PrimeButton>
</template>

<script setup lang="ts">
import { inject, onMounted, type Ref, ref } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Listbox from 'primevue/listbox';
import PrimeButton from 'primevue/button';
import Message from 'primevue/message';
import type Keycloak from 'keycloak-js';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { AxiosError } from 'axios';
import { type BasePortfolio } from '@clients/userservice';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const data = dialogRef?.value.data;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

let companyId: string;

const allUserPortfolios = ref<BasePortfolio[]>([]);
const selectedPortfolios = ref<BasePortfolio[]>([]);

const errorMessage = ref('');
const isLoading = ref(false);

onMounted(() => {
  if (!data?.companyId) return;
  companyId = data.companyId;
  allUserPortfolios.value = data.allUserPortfolios;
  errorMessage.value = '';
});

/**
 * Handles the addition of the company to the selected portfolio(s) and closes the dialog
 * unless there is an error, in which case the modal stays open and the error message is
 * displayed.
 */
async function handleCompanyAddition(): Promise<void> {
  if (selectedPortfolios.value.length === 0) return;

  isLoading.value = true;
  errorMessage.value = '';

  try {
    await Promise.all(
      selectedPortfolios.value.map((selectedPortfolio) => {
        const updatedCompanyIds = [...new Set([...selectedPortfolio.companyIds, companyId])];

        return apiClientProvider.apiClients.portfolioController.replacePortfolio(selectedPortfolio.portfolioId, {
          portfolioName: selectedPortfolio.portfolioName,
          // as unknown as Set<string> cast required to ensure proper json is created
          companyIds: updatedCompanyIds as unknown as Set<string>,
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
 * Resets selectedPortfolios and errorMessage to their initial values, then closes the dialog.
 */
function closeDialog(): void {
  selectedPortfolios.value = [];
  errorMessage.value = '';
  dialogRef?.value.close();
}
</script>

<style scoped lang="scss">
@use '@/assets/scss/colors';
@use '@/assets/scss/variables';

:deep(.p-listbox-item) {
  max-width: 17rem;
  padding: 0 variables.$spacing-xxxs; // only horizontal padding
  border-top: variables.$spacing-xxxs solid transparent;
  border-bottom: variables.$spacing-xxxs solid transparent;
  background-clip: padding-box; // background color only applies to padding-box area, not the border
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.p-listbox-item:hover) {
  color: colors.$fill-dropdown-hover-text;
  background-color: colors.$fill-dropdown-hover-bg;
}

:deep(.p-listbox-item.p-highlight) {
  color: colors.$fill-dropdown-select-text-color;
  background-color: colors.$fill-dropdown-select-bg-color;
}
</style>
