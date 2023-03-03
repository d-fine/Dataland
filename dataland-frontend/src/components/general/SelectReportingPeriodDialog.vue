<template>
  <DataTable responsiveLayout="scroll" :value="reportingPeriodDataTableContents">
    <Column field="reportingPeriod" header="REPORTING PERIOD" :sortable="true"></Column>
    <Column field="editUrl" header="">
      <template #body="{ data }">
        <router-link :to="data.editUrl">EDIT</router-link>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { DataMetaInformation } from "@clients/backend";
import DataTable from "primevue/datatable";
import Column from "primevue/column";

interface ReportingPeriodTableEntry {
  reportingPeriod: string;
  editUrl: string;
}

export default defineComponent({
  inject: ["dialogRef"],
  name: "DetailsCompanyDataTable",
  components: { DataTable, Column },
  data() {
    return {
      mapOfReportingPeriodToActiveDataset: new Map<string, DataMetaInformation>(),
    };
  },
  computed: {
    reportingPeriodDataTableContents(): Array<ReportingPeriodTableEntry> {
      const dataTableContents = [];
      for (const [key, value] of this.mapOfReportingPeriodToActiveDataset) {
        dataTableContents.push({
          reportingPeriod: key,
          editUrl: `/companies/${value.companyId}/frameworks/${value.dataType}/upload?templateDataId=${value.dataId}`,
        });
      }
      return dataTableContents;
    },
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      mapOfReportingPeriodToActiveDataset: Map<string, DataMetaInformation>;
    };
    this.mapOfReportingPeriodToActiveDataset = dialogRefData.mapOfReportingPeriodToActiveDataset;
  },
  methods: {},
});
</script>
