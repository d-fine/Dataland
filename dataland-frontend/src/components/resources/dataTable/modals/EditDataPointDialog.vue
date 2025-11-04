<template>
  <BigDecimalExtendedDataPointFormFieldDialog v-model:apiBody="apiBody" />
  <div style="display: flex; justify-content: flex-end">
    <PrimeButton
      label="SAVE CHANGES"
      icon="pi pi-save"
      style="margin-top: var(--spacing-md)"
      @click="updateDataPoint"
    />
  </div>
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
import type { UploadedDataPoint } from '@clients/backend';
const apiBody = ref<UploadedDataPoint>({} as UploadedDataPoint);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const errorMessage = ref('');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const companyId = dialogRef?.value?.data?.companyId;
const reportingPeriod = dialogRef?.value?.data?.reportingPeriod;
const dataId = dialogRef?.value?.data?.dataId as string;
const emit = defineEmits<{
  dataUpdated: [];
}>();
provide('companyId', companyId as string);
provide('reportingPeriod', reportingPeriod as string);
provide('dataId', dataId);

/**
 * Updates the data point with the current API body.
 */
async function updateDataPoint(): Promise<void> {
  await apiClientProvider.apiClients.dataPointController.postDataPoint(apiBody.value, true).catch((error) => {
    console.error(error);
    errorMessage.value = error.message;
  });
  dialogRef?.value?.close({ dataUpdated: true });
  console.log('EditDataPointDialog: emitting dataUpdated');
  emit('dataUpdated')
}
</script>
