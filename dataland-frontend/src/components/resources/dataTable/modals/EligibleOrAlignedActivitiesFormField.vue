<template>
  <ActivitiesEditTable
    ref="tableRef"
    :extendedDataPointObject="props.extendedDataPointObject"
    amountHeader="Aligned amount"
    :percentFields="percentFields"
    :yesNoFields="yesNoFields"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import ActivitiesEditTable from '@/components/resources/dataTable/modals/ActivitiesEditTable.vue';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import type { ActivityField } from '@/utils/ActivityListHelpers.ts';
import { substantialContributionPercentFields } from '@/components/resources/dataTable/modals/ActivityFieldDefinitions.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const percentFields: ActivityField[] = [
  { path: 'share.relativeShareInPercent', header: 'Aligned share (%)' },
  { path: 'relativeEligibleShareInPercent', header: 'Eligible share (%)' },
  ...substantialContributionPercentFields,
];
const yesNoFields: ActivityField[] = [
  { path: 'enablingActivity', header: 'Enabling activity' },
  { path: 'transitionalActivity', header: 'Transitional activity' },
];

const tableRef = ref<{ buildApiBodyWithExtendedInfo: () => string; isPageValid: boolean }>();
const isPageValid = computed<boolean>(() => tableRef.value?.isPageValid ?? true);

/**
 * @returns the JSON-stringified API body
 */
function buildApiBodyWithExtendedInfo(): string {
  return tableRef.value?.buildApiBodyWithExtendedInfo() ?? '';
}

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>
