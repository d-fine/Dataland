<template>
  <div v-if="!displaySuccessMessage" class="container">
    <MultiSelect
      v-model="selectedPortfolios"
      :options="allUserPortfolios"
      optionLabel="portfolioName"
      placeholder="Select Portfolios"
      :showToggleAll="false"
      :disabled="isLoading"
      data-test="portfolioSelectionMultiSelect"
      :max-selected-labels="0"
      :selected-items-label="selectedItemsLabel"
      :pt="{
        option: {
          style: 'max-width: 15rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;',
        },
      }"
    >
      <template #dropdownicon>
        <svg class="ml-2" xmlns="http://www.w3.org/2000/svg" width="10" height="7" xml:space="preserve">
          <polygon points="0,0 5,5 10,0" fill="currentColor" />
        </svg>
      </template>
    </MultiSelect>

    <p class="gray-text font-italic text-xs m-0 pb-2">
      Choose one or more portfolios to add this company to. Portfolios already containing it will not be modified.
    </p>

    <Message v-if="errorMessage" severity="error" class="my-3">
      {{ errorMessage }}
    </Message>

    <PrimeButton
      class="primary-button primary-button-in-modal"
      aria-label="Add Company"
      :disabled="selectedPortfolios.length === 0 || isLoading"
      :loading="isLoading"
      @click="handleCompanyAddition"
      data-test="saveButton"
    >
      <i class="pi pi-plus pr-2" />
      <span v-if="selectedPortfolios.length <= 1">Add company to portfolio</span>
      <span v-else>Add company to portfolios</span>
    </PrimeButton>
  </div>
  <SuccessMessage v-else success-message="Successfully added!" :closable="false" />
</template>

<script setup lang="ts">
import { computed, inject, onMounted, type Ref, ref } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import MultiSelect from 'primevue/multiselect';
import PrimeButton from 'primevue/button';
import Message from 'primevue/message';
import type Keycloak from 'keycloak-js';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { AxiosError } from 'axios';
import { type BasePortfolio } from '@clients/userservice';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';

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

const selectedItemsLabel = computed(() => {
  return `{0} item ${selectedPortfolios.value.length > 1 ? 's' : ''} selected`;
});

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
  }, 2000);
}
</script>

<style scoped lang="scss">
@use '@/assets/scss/colors';
@use '@/assets/scss/variables';

.container {
  width: 28em;
  border-radius: 0.25rem;
  background-color: white;
  padding: 0 1.5rem 1rem;
  align-items: center;
}

:deep(.primary-button-in-modal) {
  width: 70%;
  margin: 0 15%;
}

:deep(.p-badge) {
  background: #fff;
  color: #5a4f36;
}

:deep(.p-multiselect) {
  background: none;
  box-shadow: none;
  margin: variables.$spacing-sm 28%;
  border-radius: 0.5rem;
  padding: 0.5rem;
  border: 3px solid black;
}

:deep(.p-multiselect-trigger) {
  width: auto;
}

:deep(.selection-button) {
  background: white;
  color: #5a4f36;
  border: 2px solid #5a4f36;
  border-radius: 8px;
  height: 2.5rem;

  .selection-button-content {
    margin: 0.5rem 1rem;
  }

  &.overlayVisible {
    background: #e0dfde;
  }

  &.filterActive {
    background: #5a4f36;
    color: white;
  }
}
</style>
