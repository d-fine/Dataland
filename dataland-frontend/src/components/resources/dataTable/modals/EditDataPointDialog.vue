<template>
  <component
    :is="resolvedComponent"
    v-model:apiBody="apiBody"
    :extendedDataPointObject="extendedDataPointObject"
    :reportingPeriod="reportingPeriod"
    :dataPointTypeId="dataPointTypeId"
    :value="value"
  />
  <Message v-if="errorMessage" severity="error" :life="3000">
    {{ errorMessage }}
  </Message>
  <div style="display: flex; justify-content: flex-end; margin-top: var(--spacing-md)">
    <PrimeButton label="SAVE CHANGES" icon="pi pi-save" @click="updateDataPoint" data-test="save-data-point-button" />
  </div>
</template>

<script setup lang="ts">
import { inject, type Ref, ref, computed, unref, type Component, provide } from 'vue';
import PrimeButton from 'primevue/button';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import type { UploadedDataPoint } from '@clients/backend';
import { componentDictionary } from '@/components/resources/dataTable/EditDataPointComponentDictionary.ts';
import Message from 'primevue/message';
import { AxiosError } from 'axios';
import {DataPointObject} from "@/components/resources/dataTable/conversion/Utils.ts";

const apiBody = ref<UploadedDataPoint>({} as UploadedDataPoint);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const errorMessage = ref('');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const companyId = dialogRef?.value?.data?.companyId;
const reportingPeriod = dialogRef?.value?.data?.reportingPeriod;
const uploadComponentName = dialogRef?.value?.data?.uploadComponentName;
const dataPointTypeId = dialogRef?.value?.data?.dataPointTypeId;
const dataPoint = dialogRef?.value?.data?.dataPoint;
const emit = defineEmits<(e: 'dataUpdated') => void>();
const resolvedComponent = computed<Component | null>(() => {
  return componentDictionary[uploadComponentName ?? ''] ?? null;
});

const value = unref(dataPoint?.displayValue?.innerContents?.displayValue).trim() ?? '';

const extendedDataPointObject: DataPointObject = {
  quality: unref(dataPoint?.displayValue?.quality ?? ''),
  comment: unref(dataPoint?.displayValue?.comment ?? ''),
  dataSource: {
    fileName: unref(dataPoint?.displayValue?.dataSource?.fileName ?? ''),
    page: unref(dataPoint?.displayValue?.dataSource?.page ?? ''),
  },
};

console.log("DATAPOINT FROM EDIT MODAL", extendedDataPointObject);

provide('companyId', companyId);

/**
 * Updates the data point with the current API body.
 */
async function updateDataPoint(): Promise<void> {
  errorMessage.value = '';
  try {
    await apiClientProvider.apiClients.dataPointController.postDataPoint(apiBody.value, true);
    dialogRef?.value?.close({ dataUpdated: true });
    emit('dataUpdated');
  } catch (error) {
    if (error instanceof AxiosError) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = 'Failed to edit Data Point.';
    }
  }
}
</script>
