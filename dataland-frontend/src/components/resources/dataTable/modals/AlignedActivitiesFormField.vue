<template>
  <ActivitiesEditTable ref="tableRef" :extendedDataPointObject="props.extendedDataPointObject" :groups="groups" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import ActivitiesEditTable from '@/components/resources/dataTable/modals/ActivitiesEditTable.vue';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import type { ActivityFieldGroup } from '@/utils/ActivityListHelpers.ts';
import {
  dnshGroup,
  enablingAndTransitionalGroups,
  kpiGroupForAlignedOrNonAligned,
  minimumSafeguardsGroup,
  substantialContributionGroup,
} from '@/components/resources/dataTable/modals/ActivityFieldDefinitions.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const groups: ActivityFieldGroup[] = [
  kpiGroupForAlignedOrNonAligned,
  substantialContributionGroup,
  dnshGroup,
  minimumSafeguardsGroup,
  ...enablingAndTransitionalGroups,
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
