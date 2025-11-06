<template>
  <component
    :is="resolvedComponent"
    v-model:apiBody="apiBody"
    :value="value"
    :insertedComment="comment"
    :selectedDocument="fileName"
    :insertedPage="page"
    :chosenQuality="quality"
  />
  <div style="display: flex; justify-content: flex-end">
    <PrimeButton
      label="SAVE CHANGES"
      icon="pi pi-save"
      style="margin-top: var(--spacing-md)"
      @click="updateDataPoint"
      data-test="save-data-point-button"
    />
  </div>
</template>

<script setup lang="ts">
import { inject, type Ref, ref, provide, computed, unref, type Component } from 'vue';
import PrimeButton from 'primevue/button';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import type { UploadedDataPoint } from '@clients/backend';
import { componentDictionary } from '@/components/resources/dataTable/EditDataPointComponentDictionary.ts';

const apiBody = ref<UploadedDataPoint>({} as UploadedDataPoint);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const errorMessage = ref('');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const companyId = dialogRef?.value?.data?.companyId;
const reportingPeriod = dialogRef?.value?.data?.reportingPeriod;
const dataId = dialogRef?.value?.data?.dataId as string;
const uploadComponentName = dialogRef?.value?.data?.uploadComponentName;
const dataPointTypeId = dialogRef?.value?.data?.dataPointTypeId;
const data = dialogRef?.value?.data?.dataPoint;
const emit = defineEmits<(e: 'dataUpdated') => void>();
provide('companyId', companyId as string);
provide('reportingPeriod', reportingPeriod as string);
provide('dataId', dataId);
provide('dataPointTypeId', dataPointTypeId);

const resolvedComponent = computed<Component | null>(() => {
  return componentDictionary[uploadComponentName ?? ''] ?? null;
});

/**
 * Updates the data point with the current API body.
 */
async function updateDataPoint(): Promise<void> {
  await apiClientProvider.apiClients.dataPointController.postDataPoint(apiBody.value, true).catch((error) => {
    console.error(error);
    errorMessage.value = error.message;
  });
  dialogRef?.value?.close({ dataUpdated: true });
  emit('dataUpdated');
}

const value = unref(data?.displayValue?.innerContents?.displayValue).trim() ?? '';
const fileName = unref(data?.displayValue?.dataSource?.fileName ?? '');
const page = unref(data?.displayValue?.dataSource?.page ?? '');
const quality = unref(data?.displayValue?.quality ?? '');
const comment = unref(data?.displayValue?.comment ?? '');

</script>
