<template>
  <DataTable :value="listOfRowContents" class="activities-data-table">
    <Column field="activity" header="Activity">
      <template #body="{ data }">
        {{ data.activity }}
      </template>
    </Column>
    <Column field="ccmCca" header="CCM + CCA">
      <template #body="{ data }">
        {{ data.ccmCca || '' }}
      </template>
    </Column>
    <Column field="ccm" header="CCM">
      <template #body="{ data }">
        {{ data.ccm || '' }}
      </template>
    </Column>
    <Column field="cca" header="CCA">
      <template #body="{ data }">
        {{ data.cca || '' }}
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { ActivityName } from '@/components/resources/frameworkDataSearch/nuclearAndGas/NuclearAndGasActivityNames';
import { NuclearAndGasAlignedDenominator, NuclearAndGasAlignedNumerator, NuclearAndGasEligibleButNotAligned, NuclearAndGasNonEligible } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model';

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
        const activityName = ActivityName[fieldName] as string;
        const objective = rawData[fieldName as keyof NuclearAndGasAlignedDenominator];

        if (!objective) return null;

        return {
          activity: activityName,
          ccmCca: objective.mitigationAndAdaption ?? null,
          ccm: objective.mitigation ?? null,
          cca: objective.adaption ?? null,
        };
      })
      .filter((row) => row !== null);
    },
  },
});
</script>
