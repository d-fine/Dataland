<template>
  <DataTable data-test="dataTableTest" :value="listOfRowContents" class="activities-data-table">
    <Column v-for="col of mainColumnDefinitions" :field="col.field" :key="col.field" :header="col.header">
      <template #body="{ data }">
        {{ data[col.field] }}
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
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
import type { NuclearAndGasEnvironmentalObjective } from '@clients/qaservice';

interface DialogRefData {
  input: NuclearAndGasType;
  columnHeaders: {};
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

export default defineComponent({
  name: 'TaxonomyShareDataTable',
  components: { DataTable, Column },
  inject: ['dialogRef'],
  data() {
    return {
      nuclearAndGasData: {} as NuclearAndGasType,
      columnHeaders: {} as { [key: string]: [columnName: string] },
      listOfRowContents: [] as NuclearAndGasRowDefinition[],
    };
  },
  computed: {
    dialogRefData(): DialogRefData {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      return dialogRefToDisplay.data as DialogRefData;
    },
    mainColumnDefinitions() {
      if (isNonEligible(this.nuclearAndGasData)) {
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
    },
  },
  mounted() {
    this.nuclearAndGasData = this.dialogRefData.input;
    this.columnHeaders = this.dialogRefData.columnHeaders;
    this.generateRowContents();
  },
  methods: {
    /**
     * Creates the rows to be displayed in the data table from the nuclearAndGasData input and populates the component's
     * listOfRowContents
     */
    generateRowContents() {
      if (!this.nuclearAndGasData) {
        return;
      }

      let listOfActivities: NuclearAndGasActivityDescription[];
      if (isAlignedDenominator(this.nuclearAndGasData))
        listOfActivities = nuclearAndGasActivityNames.alignedDenominator;
      else if (isAlignedNumerator(this.nuclearAndGasData))
        listOfActivities = nuclearAndGasActivityNames.alignedNumerator;
      else if (isEligibleButNotAlignedNumerator(this.nuclearAndGasData))
        listOfActivities = nuclearAndGasActivityNames.eligibleButNotAligned;
      else if (isNonEligible(this.nuclearAndGasData)) listOfActivities = nuclearAndGasActivityNames.nonEligible;

      Object.keys(this.nuclearAndGasData).forEach((key, idx) => {
        if (isNonEligible(this.nuclearAndGasData)) {
          this.listOfRowContents.push({
            economicActivity: listOfActivities[idx].description,
            proportion: formatPercentageNumberAsString(this.nuclearAndGasData[key as keyof NuclearAndGasNonEligible]),
          });
        } else {
          const objective = this.nuclearAndGasData[
            key as keyof typeof this.nuclearAndGasData
          ] as NuclearAndGasEnvironmentalObjective;
          this.listOfRowContents.push({
            economicActivity: listOfActivities[idx].description,
            mitigation: formatPercentageNumberAsString(objective?.mitigation),
            adaptation: formatPercentageNumberAsString(objective?.adaptation),
            mitigationAndAdaptation: formatPercentageNumberAsString(objective?.mitigationAndAdaptation),
          });
        }
      });

      this.listOfRowContents = this.listOfRowContents.filter((item) => {
        return item.proportion || item.mitigation || item.adaptation || item.mitigationAndAdaptation;
      });
    },
  },
});
</script>
