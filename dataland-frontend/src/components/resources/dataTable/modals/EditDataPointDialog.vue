<template>
  <component ref="componentRef" :is="resolvedComponent" :extendedDataPointObject="extendedDataPointObject" />
  <Message v-if="errorMessage" severity="error" :life="3000">
    {{ errorMessage }}
  </Message>
  <Message v-if="pendingActivationMessage" severity="info" data-test="save-data-point-pending-activation">
    {{ pendingActivationMessage }}
  </Message>
  <div style="display: flex; justify-content: flex-end; margin-top: var(--spacing-md)">
    <PrimeButton
      label="SAVE CHANGES"
      icon="pi pi-save"
      @click="updateDataPoint"
      data-test="save-data-point-button"
      :disabled="isPending || !isPageValid"
    />
  </div>
</template>

<script setup lang="ts">
import { inject, type Ref, ref, computed, unref, type Component, provide } from 'vue';
import PrimeButton from 'primevue/button';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { QualityOptions } from '@clients/backend';
import { componentDictionary } from '@/components/resources/dataTable/EditDataPointComponentDictionary.ts';
import Message from 'primevue/message';
import { AxiosError } from 'axios';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import { useSaveDataPointAwaitingActivation } from '@/utils/useSaveDataPointAwaitingActivation.ts';
import { activityComponentNames, kpiTypeFromDataPointTypeId } from '@/utils/ActivityListHelpers.ts';

const { save: saveDataPoint, isPending } = useSaveDataPointAwaitingActivation();
const errorMessage = ref('');
const pendingActivationMessage = ref('');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const companyId = dialogRef?.value?.data?.companyId;
const reportingPeriod = dialogRef?.value?.data?.reportingPeriod;
const uploadComponentName = dialogRef?.value?.data?.uploadComponentName;
const dataPointTypeId = dialogRef?.value?.data?.dataPointTypeId;
const dataPoint = dialogRef?.value?.data?.dataPoint;
const resolvedComponent = computed<Component | null>(() => {
  return componentDictionary[uploadComponentName ?? ''] ?? null;
});

const componentRef = ref<{ buildApiBodyWithExtendedInfo: () => string; isPageValid: boolean }>();

/**
 * Whether the page reference currently entered in the resolved form component is valid.
 */
const isPageValid = computed<boolean>(() => componentRef.value?.isPageValid ?? true);

const modalData = dataPoint?.displayValue?.modalOptions?.data;

const isActivityListDataPoint = activityComponentNames.has(uploadComponentName ?? '');

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
  const qualityOptionValues = Object.values(QualityOptions) as string[];
  const displayText = innerDisplayText();
  const isQuality = qualityOptionValues.includes(displayText);

  return {
    value: displayText && !isQuality ? displayText : undefined,
    quality: isQuality ? displayText : unref(dataPoint?.displayValue?.quality ?? ''),
    comment: unref(dataPoint?.displayValue?.comment ?? ''),
    dataSource: {
      fileReference: unref(dataPoint?.displayValue?.dataSource?.fileReference ?? ''),
      page: unref(dataPoint?.displayValue?.dataSource?.page ?? ''),
    },
  };
}

const extendedDataPointObject: ExtendedDataPointType = isActivityListDataPoint
  ? buildActivityListDataPointObject()
  : buildScalarDataPointObject();

provide('companyId', companyId);
// For empty cells there is no modal data, so the KPI type is derived from the data point type id instead.
provide('activityKpiType', modalData?.kpiType ?? kpiTypeFromDataPointTypeId(dataPointTypeId));

/**
 * Updates the data point with the current API body.
 */
function updateDataPoint(): void {
  errorMessage.value = '';
  pendingActivationMessage.value = '';
  saveDataPoint(
    {
      uploadedDataPoint: {
        dataPoint: componentRef.value?.buildApiBodyWithExtendedInfo() ?? '',
        dataPointType: dataPointTypeId,
        companyId: companyId,
        reportingPeriod: reportingPeriod,
      },
      bypassQa: true,
    },
    {
      onDone: ({ activated }) => {
        if (!activated) {
          pendingActivationMessage.value =
            'Your change was saved, but we could not confirm it yet. Please close this dialog and reload the page to check.';
          return;
        }
        dialogRef?.value?.close({ dataUpdated: true });
      },
      onError: (error) => {
        errorMessage.value = error instanceof AxiosError ? error.message : 'Failed to edit Data Point.';
      },
    }
  );
}
</script>
