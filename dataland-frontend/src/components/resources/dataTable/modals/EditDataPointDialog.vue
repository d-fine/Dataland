<template>
  <component ref="componentRef" :is="resolvedComponent" :extendedDataPointObject="extendedDataPointObject" />
  <Message v-if="errorMessage" severity="error" :life="3000">
    {{ errorMessage }}
  </Message>
  <div style="display: flex; justify-content: flex-end; margin-top: var(--spacing-md)">
    <PrimeButton
      label="SAVE CHANGES"
      icon="pi pi-save"
      @click="updateDataPoint"
      data-test="save-data-point-button"
      :disabled="buttonDisabled || !isPageValid"
    />
  </div>
</template>

<script setup lang="ts">
import { inject, type Ref, ref, computed, unref, type Component, provide } from 'vue';
import PrimeButton from 'primevue/button';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { QualityOptions, type UploadedDataPoint } from '@clients/backend';
import { componentDictionary } from '@/components/resources/dataTable/EditDataPointComponentDictionary.ts';
import Message from 'primevue/message';
import { AxiosError } from 'axios';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import { activityComponentNames, kpiTypeFromDataPointTypeId } from '@/utils/ActivityListHelpers.ts';

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
const buttonDisabled = ref(false);

const componentRef = ref<{ buildApiBodyWithExtendedInfo: () => string; isPageValid: boolean }>();

/**
 * Whether the page reference currently entered in the resolved form component is valid.
 */
const isPageValid = computed<boolean>(() => componentRef.value?.isPageValid ?? true);

const modalData = dataPoint?.displayValue?.modalOptions?.data;

/**
 * Activity list data points are edited as a list. For empty cells there is no modal data, so the list structure is
 * built with empty values.
 * @returns whether the data point is an activity list
 */
function isActivityListDataPoint(): boolean {
  return activityComponentNames.has(uploadComponentName ?? '');
}

/**
 * @returns the extended data point object for activity lists (empty if there is no modal data)
 */
function buildActivityListDataPointObject(): ExtendedDataPointType {
  return {
    value: modalData?.listOfRowContents ?? [],
    quality: modalData?.dataPointDisplay?.quality ?? '',
    comment: modalData?.dataPointDisplay?.comment ?? '',
    dataSource: {
      fileReference: modalData?.dataPointDisplay?.dataSource?.fileReference ?? '',
      page: modalData?.dataPointDisplay?.dataSource?.page ?? '',
    },
  };
}

/**
 * @returns the inner display value as trimmed string (empty if it is not a string)
 */
function innerDisplayText(): string {
  const inner = unref(dataPoint?.displayValue?.innerContents?.displayValue ?? '');
  return typeof inner === 'string' ? inner.trim() : '';
}

/**
 * @returns the extended data point object for scalar data points
 */
function buildScalarDataPointObject(): ExtendedDataPointType {
  return {
    value: ((): string | undefined => {
      const value = innerDisplayText() || undefined;
      return value && !(Object.values(QualityOptions) as string[]).includes(value) ? value : undefined;
    })(),
    quality: ((): string | undefined => {
      const valueOrPotentialQuality = innerDisplayText() || undefined;
      return valueOrPotentialQuality && (Object.values(QualityOptions) as string[]).includes(valueOrPotentialQuality)
        ? valueOrPotentialQuality
        : unref(dataPoint?.displayValue?.quality ?? '');
    })(),
    comment: unref(dataPoint?.displayValue?.comment ?? ''),
    dataSource: {
      fileReference: unref(dataPoint?.displayValue?.dataSource?.fileReference ?? ''),
      page: unref(dataPoint?.displayValue?.dataSource?.page ?? ''),
    },
  };
}

const extendedDataPointObject: ExtendedDataPointType = isActivityListDataPoint()
  ? buildActivityListDataPointObject()
  : buildScalarDataPointObject();

provide('companyId', companyId);
// For empty cells there is no modal data, so the KPI type is derived from the data point type id instead.
provide('activityKpiType', modalData?.kpiType ?? kpiTypeFromDataPointTypeId(dataPointTypeId));

/**
 * Updates the data point with the current API body.
 */
async function updateDataPoint(): Promise<void> {
  errorMessage.value = '';
  buttonDisabled.value = true;
  try {
    const apiBody: UploadedDataPoint = {
      dataPoint: componentRef.value?.buildApiBodyWithExtendedInfo() ?? '',
      dataPointType: dataPointTypeId,
      companyId: companyId,
      reportingPeriod: reportingPeriod,
    };
    await apiClientProvider.apiClients.dataPointController.postDataPoint(apiBody, true);
    dialogRef?.value?.close({ dataUpdated: true });
    setTimeout(() => {
      emit('dataUpdated');
    }, 2000);
  } catch (error) {
    if (error instanceof AxiosError) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = 'Failed to edit Data Point.';
    }
  } finally {
    buttonDisabled.value = false;
  }
}
</script>
