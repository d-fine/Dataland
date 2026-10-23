<template>
  <ActivitiesEditTable
    ref="tableRef"
    :extendedDataPointObject="props.extendedDataPointObject"
    amountHeader="Non-Aligned Amount"
    :groups="groups"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import ActivitiesEditTable from '@/components/resources/dataTable/modals/ActivitiesEditTable.vue';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import type { ActivityFieldGroup } from '@/utils/ActivityListHelpers.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const groups: ActivityFieldGroup[] = [
  { key: 'share', label: '', fields: [{ path: 'share.relativeShareInPercent', header: 'Share (%)', type: 'percent' }] },
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
