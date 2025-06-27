<template>
  <DataTable data-test="dataTableTest" :value="listOfRowContents" class="activities-data-table">
    <Column
      v-for="col of mainColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      bodyClass="col-percentage"
    >
      <template #body="{ data }">
        {{ data[col.field] }}
      </template>
    </Column>
  </DataTable>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, ref, type Ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { nuclearAndGasActivityNames } from '@/components/resources/frameworkDataSearch/nuclearAndGas/NuclearAndGasActivityNames';
import type { NuclearAndGasNonEligible } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';
import { formatPercentageNumberAsString } from '@/utils/Formatter';
import {
  isAlignedDenominator,
  isAlignedNumerator,
  isEligibleButNotAlignedNumerator,
  isNonEligible,
  type NuclearAndGasType,
} from '@/utils/NuclearAndGasUtils';
import type { NuclearAndGasEnvironmentalObjective } from '@clients/backend';

interface DialogRefData {
  input: NuclearAndGasType;
  columnHeaders: { [key: string]: [columnName: string] };
}

interface NuclearAndGasActivityDescription {
  key: string;
  label: string;
  description: string;
}

interface NuclearAndGasRowDefinition {
  economicActivity: string;
  mitigation?: string;
  adaptation?: string;
  mitigationAndAdaptation?: string;
  proportion?: string;
}

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');

const nuclearAndGasData = ref<NuclearAndGasType>({} as NuclearAndGasType);
const columnHeaders = ref<{ [key: string]: [string] }>({});
const listOfRowContents = ref<NuclearAndGasRowDefinition[]>([]);

const mainColumnDefinitions = computed(() => {
  if (isNonEligible(nuclearAndGasData.value)) {
    return [
      { field: 'economicActivity', header: 'Economic Activity' },
      { field: 'proportion', header: 'Proportion' },
    ];
  } else {
    return [
      { field: 'economicActivity', header: 'Economic Activity' },
      { field: 'mitigation', header: 'CCM' },
      { field: 'adaptation', header: 'CCA' },
      { field: 'mitigationAndAdaptation', header: 'CCM + CCA' },
    ];
  }
});

const dialogRefData = computed(() => {
  return (dialogRef?.value as DynamicDialogInstance).data as DialogRefData;
});

onMounted(() => {
  nuclearAndGasData.value = dialogRefData.value.input;
  columnHeaders.value = dialogRefData.value.columnHeaders;
  generateRowContents();
});

/**
 * Creates the rows to be displayed in the data table from the nuclearAndGasData input and populates the component's
 * listOfRowContents
 */
function generateRowContents(): void {
  if (!nuclearAndGasData.value) {
    return;
  }

  let listOfActivities: NuclearAndGasActivityDescription[];
  if (isAlignedDenominator(nuclearAndGasData.value)) listOfActivities = nuclearAndGasActivityNames.alignedDenominator;
  else if (isAlignedNumerator(nuclearAndGasData.value)) listOfActivities = nuclearAndGasActivityNames.alignedNumerator;
  else if (isEligibleButNotAlignedNumerator(nuclearAndGasData.value))
    listOfActivities = nuclearAndGasActivityNames.eligibleButNotAligned;
  else if (isNonEligible(nuclearAndGasData.value)) listOfActivities = nuclearAndGasActivityNames.nonEligible;

  Object.keys(nuclearAndGasData.value).forEach((key, idx) => {
    if (isNonEligible(nuclearAndGasData.value)) {
      listOfRowContents.value.push({
        economicActivity: listOfActivities[idx].description,
        proportion: formatPercentageNumberAsString(nuclearAndGasData.value[key as keyof NuclearAndGasNonEligible]),
      });
    } else {
      const objective = nuclearAndGasData.value[
        key as keyof typeof nuclearAndGasData.value
      ] as NuclearAndGasEnvironmentalObjective;
      listOfRowContents.value.push({
        economicActivity: listOfActivities[idx].description,
        mitigation: formatPercentageNumberAsString(objective?.mitigation),
        adaptation: formatPercentageNumberAsString(objective?.adaptation),
        mitigationAndAdaptation: formatPercentageNumberAsString(objective?.mitigationAndAdaptation),
      });
    }
  });
}
</script>
