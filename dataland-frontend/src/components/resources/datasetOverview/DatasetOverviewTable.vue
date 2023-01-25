<template>
  <div class="col-12 text-left">
    <DataTable
      :value="datasetTableInfos"
      responsive-layout="scroll"
      id="dataset-overview-table"
      class="table-cursor mt-1"
    >
      <Column field="companyName" header="COMPANY" :sortable="true"></Column>
      <Column :field="dataType" header="DATA FRAMEWORK" :sortable="true">
        <template #body="{ data }">
          {{ humanizeString(data.dataType) }}
        </template>
      </Column>
      <Column field="year" header="YEAR" :sortable="true" class="hidden"></Column>
      <Column field="status" header="STATUS" :sortable="true">
        <template #body="{ data }">
          <span :class="`p-badge badge-${data.status.color} m-0`">{{ data.status.text }}</span>
        </template>
      </Column>
      <Column field="submissionDate" header="SUBMISSION DATE" :sortable="true"></Column>
      <Column field="companyName" header="" class="d-bg-white d-datatable-column-right">
        <template #header>
          I'M A SEARCHBAR
          <!-- TODO? SEARCHBAR -->
        </template>
        <template #body="{ data }">
          <router-link :to="getRouterLinkTargetFramework(data)" class="text-primary no-underline font-bold">
            <div class="text-right">
              <span>{{ isDatasetRejected(data) ? "ADD NEW DATASET" : "VIEW" }}</span>
              <span class="ml-3">></span>
            </div>
          </router-link>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<!-- TODO does not apply when scoped. why? -->
<style>
#dataset-overview-table tr:hover {
  cursor: pointer;
}

#dataset-overview-table th {
  /*background-color: green;*/
}
</style>

<script lang="ts">
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { humanizeString } from "@/utils/StringHumanizer";
import { DatasetStatus, DatasetTableInfo } from "@/components/resources/datasetOverview/DatasetTableInfo";

export default defineComponent({
  name: "DatasetOverviewTable",
  components: {
    DataTable,
    Column,
  },
  data() {
    return {
      humanizeString: humanizeString,
    };
  },
  props: {
    datasetTableInfos: {
      type: Array,
      default: [],
    },
  },
  methods: {
    getRouterLinkTargetFramework(datasetTableInfo: DatasetTableInfo): string {
      if (this.isDatasetRejected(datasetTableInfo)) {
        return `/companies/${datasetTableInfo.companyId}/frameworks/${datasetTableInfo.dataType}/upload`;
      } else {
        return `/companies/${datasetTableInfo.companyId}/frameworks/${datasetTableInfo.dataType}`;
      }
    },
    isDatasetRejected(datasetTableEntry: DatasetTableInfo): boolean {
      return datasetTableEntry.status.text === DatasetStatus.Requested.text;
    },
  },
});
</script>
