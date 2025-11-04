<template>
  <BigDecimalExtendedDataPointFormFieldDialog
    v-model:apiBody="apiBody"
  />
  <PrimeButton label="SAVE CHANGES" icon="pi pi-save" style="margin-top: var(--spacing-md)" @click="updateDataPoint"/>
  <Message
      v-if="errorMessage"
      severity="error"
      variant="simple"
      size="small"
      data-test="reportingYearError"
  >
   {{errorMessage}}
  </Message>
</template>

<script setup lang="ts">
import {inject, type Ref, ref, provide } from 'vue';
import PrimeButton from 'primevue/button';
import BigDecimalExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/BigDecimalExtendedDataPointFormFieldDialog.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Message from 'primevue/message';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import type {UploadedDataPoint} from "@clients/backend";

const apiBody = ref<UploadedDataPoint>({} as UploadedDataPoint);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const errorMessage = ref('')
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const companyID = dialogRef?.value?.data?.companyID;
const reportingPeriod = dialogRef?.value?.data?.reportingPeriod;
provide('companyID', companyID as string);
provide('reportingPeriod', reportingPeriod as string);

/**
 * Updates the data point with the current API body.
 */
async function updateDataPoint(): Promise<void> {
  await apiClientProvider.apiClients.dataPointController.postDataPoint(
      apiBody.value,
      true
  ).catch((error) => {
    console.error(error);
    errorMessage.value = error.message
  })
}
</script>
