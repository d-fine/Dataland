<template>
  <BigDecimalExtendedDataPointFormFieldDialog v-model:value="value" />
  <PrimeButton label="SAVE CHANGES" icon="pi pi-save" style="margin-top: var(--spacing-md)" @click="updateDataPoint" />
  <Message v-if="errorMessage" severity="error" variant="simple" size="small" data-test="reportingYearError">
    {{ errorMessage }}
  </Message>
</template>

<script setup lang="ts">
import { inject, type Ref, ref, provide } from 'vue';
import PrimeButton from 'primevue/button';
import BigDecimalExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/BigDecimalExtendedDataPointFormFieldDialog.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Message from 'primevue/message';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';

const value = ref();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const errorMessage = ref('');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const companyID = dialogRef?.value?.data?.companyID;
provide('companyID', companyID as string);

/**
 * Updates a data point by sending a request to the data point controller.
 *
 * The method constructs a data point payload based on the given values and
 * calls the `postDataPoint` method of the API client with the necessary parameters.
 * In the case of an error during the API call, an error message is logged, and
 * the relevant error message is assigned to a variable.
 *
 * @return {Promise<void>} A promise that resolves when the data point is successfully updated, or rejects with an error message.
 */
async function updateDataPoint(): Promise<void> {
  const dataPointPayload = JSON.stringify({ value: value.value });
  await apiClientProvider.apiClients.dataPointController
    .postDataPoint(
      {
        dataPoint: dataPointPayload,
        dataPointType: 'extendedDecimalEstimatedMarketCapitalizationInEUR',
        companyId: companyID,
        reportingPeriod: '2024',
      },
      true
    )
    .catch((error) => {
      console.error(error);
      errorMessage.value = error.message;
    });
}
</script>
