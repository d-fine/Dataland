<template>
  <div v-if="!displaySuccessMessage" class="container">
    <Message v-if="arePortfoliosError && !allUserPortfolios" severity="error" data-test="portfoliosErrorMessage">
      Your portfolios could not be loaded.
    </Message>
    <Listbox
      v-model="selectedPortfolios"
      :options="allUserPortfolios ?? []"
      optionLabel="portfolioName"
      multiple
      :disabled="isSaving || arePortfoliosPending"
      data-test="portfolioSelectionListbox"
      :pt="{
        option: {
          style: 'max-width: 100%; width: 100%, overflow: hidden; text-overflow: ellipsis; white-space: nowrap;',
        },
        list: {
          style: 'width: 100%;',
        },
      }"
      style="width: 100%; margin-bottom: var(--spacing-xs)"
    />

    <p class="dataland-info-text small">
      Choose one or more portfolios you wish to add this company to. Portfolios already containing the company will not
      be modified.
    </p>

    <Message v-if="errorMessage" severity="error" class="my-3">
      {{ errorMessage }}
    </Message>

    <PrimeButton
      :disabled="selectedPortfolios.length === 0 || isSaving"
      :loading="isSaving"
      @click="handleCompanyAddition"
      data-test="saveButton"
      icon="pi pi-plus"
      :label="'Add company to portfolio' + (selectedPortfolios.length > 1 ? 's' : '')"
    />
  </div>
  <Message v-else severity="success" size="large" class="container">Successfully added!</Message>
</template>

<script setup lang="ts">
import { useGetAllPortfoliosForCurrentUserQuery } from '@/api-queries/user-service/portfolio/useGetAllPortfoliosForCurrentUserQuery';
import { useReplacePortfolioMutation } from '@/api-queries/user-service/portfolio/useReplacePortfolioMutation';
import { type BasePortfolio } from '@clients/userservice';
import { AxiosError } from 'axios';
import PrimeButton from 'primevue/button';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import Message from 'primevue/message';
import Listbox from 'primevue/listbox';
import { inject, type Ref, ref } from 'vue';

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const companyId: string | undefined = dialogRef?.value.data?.companyId;

const {
  data: allUserPortfolios,
  isPending: arePortfoliosPending,
  isError: arePortfoliosError,
} = useGetAllPortfoliosForCurrentUserQuery();
const { mutateAsync: replacePortfolio } = useReplacePortfolioMutation();

const selectedPortfolios = ref<BasePortfolio[]>([]);

const errorMessage = ref('');
// The company is added to several portfolios with parallel mutations, so a single mutation state does not cover them.
const isSaving = ref(false);
const displaySuccessMessage = ref(false);

/**
 * Handles the addition of the company to the selected portfolio(s) and closes the dialog
 * unless there is an error, in which case the modal stays open and the error message is
 * displayed.
 */
async function handleCompanyAddition(): Promise<void> {
  if (selectedPortfolios.value.length === 0 || !companyId) return;

  isSaving.value = true;

  try {
    await Promise.all(
      selectedPortfolios.value.map((selectedPortfolio) => {
        const updatedCompanyIdentifiers = [...new Set([...selectedPortfolio.identifiers, companyId])];

        return replacePortfolio({
          portfolioId: selectedPortfolio.portfolioId,
          portfolioUpload: {
            portfolioName: selectedPortfolio.portfolioName,
            // as unknown as Set<string> cast required to ensure proper json is created
            identifiers: updatedCompanyIdentifiers as unknown as Set<string>,
            isMonitored: selectedPortfolio.isMonitored,
            monitoredFrameworks: selectedPortfolio.monitoredFrameworks,
            notificationFrequency: selectedPortfolio.notificationFrequency,
            timeWindowThreshold: selectedPortfolio.timeWindowThreshold,
            sharedUserIds: selectedPortfolio.sharedUserIds,
          },
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
    isSaving.value = false;
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
