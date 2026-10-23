<template>
  <ActivitiesEditTable
    ref="tableRef"
    :extendedDataPointObject="props.extendedDataPointObject"
    amountHeader="Non-aligned amount"
    :percentFields="percentFields"
    :yesNoFields="yesNoFields"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import ActivitiesEditTable from '@/components/resources/dataTable/modals/ActivitiesEditTable.vue';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import type { ActivityField } from '@/utils/ActivityListHelpers.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const percentFields: ActivityField[] = [{ path: 'share.relativeShareInPercent', header: 'Share (%)' }];
const yesNoFields: ActivityField[] = [];

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
