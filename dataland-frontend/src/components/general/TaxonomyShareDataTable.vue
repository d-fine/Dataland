<template>
  <div v-if="listOfRowContents.length > 0">
    <DataTable :value="listOfRowContents" class="activities-data-table">
      <Column field="activity" header="Economic activitiy">
        <template #body="{ data }">
          {{ data.activity }}
        </template>
      </Column>
      <Column field="ccmCca" :header="isNonEligible ? 'Proportion' : 'CCM + CCA'">
        <template #body="{ data }">
          {{ data.ccmCca || '' }}
        </template>
      </Column>
      <Column v-if="!isNonEligible" field="ccm" header="CCM">
        <template #body="{ data }">
          {{ data.ccm || '' }}
        </template>
      </Column>
      <Column v-if="!isNonEligible" field="cca" header="CCA">
        <template #body="{ data }">
          {{ data.cca || '' }}
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import {type DynamicDialogInstance} from 'primevue/dynamicdialogoptions';
import {
  ActivityName
} from '@/components/resources/frameworkDataSearch/nuclearAndGas/NuclearAndGasActivityNames';
import {
  NuclearAndGasAlignedDenominator,
  NuclearAndGasAlignedNumerator,
  NuclearAndGasEligibleButNotAligned,
  NuclearAndGasNonEligible
} from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';
import {formatPercentageNumberAsString} from "@/utils/Formatter";

interface DialogRefData {
  header: string;
  values: NuclearAndGasAlignedDenominator
      | NuclearAndGasAlignedNumerator
      | NuclearAndGasEligibleButNotAligned
      | NuclearAndGasNonEligible
}

export default defineComponent({
  name: 'TaxonomyShareDataTable',
  components: {DataTable, Column},
  inject: ['dialogRef'],
  computed: {
    dialogRefData(): DialogRefData {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      return dialogRefToDisplay.data as DialogRefData;
    },
    isNonEligible() {
      return this.dialogRefData.values && 'taxonomyNonEligibleShareNAndG426' in this.dialogRefData.values;
    },
    listOfRowContents() {
      const rawData = this.dialogRefData.values;
      return this.generateRowContents(rawData);
    },
  },
  methods: {
    generateRowContents(rawData: any) {
      if (!rawData) {
        return [];
      }

      return Object.keys(ActivityName)
      .map((key) => {
        const fieldName = key as keyof typeof ActivityName;
        const objective = rawData[fieldName as keyof NuclearAndGasAlignedDenominator | keyof NuclearAndGasNonEligible];

        if (!objective) return null;

        const activityName = ActivityName[fieldName] as string;
        const ccmCca = formatPercentageNumberAsString(this.isNonEligible ? objective : objective?.mitigationAndAdaption ?? null);
        const ccm = formatPercentageNumberAsString(objective?.mitigation ?? null);
        const cca = formatPercentageNumberAsString(objective?.adaption ?? null);

        if (!ccmCca && !ccm && !cca) {
          return null;
        }

        return {
          activity: activityName,
          ccmCca,
          ccm,
          cca
        };
      })
      .filter((row) => row !== null);
    },
  },
});
</script>
