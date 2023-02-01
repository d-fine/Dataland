<template>
  <div class="col-12 text-left">
    <DataTable
      :value="displayedDatasetTableInfos"
      id="dataset-overview-table"
      class="table-cursor mt-1"
      :paginator="true"
      :rows="100"
      :alwaysShowPaginator="false"
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
      currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
      :rowHover="true"
      @row-click="rerouteRowClick"
    >
      <Column field="companyName" header="COMPANY" :sortable="true" class="w-3"></Column>
      <Column field="dataType" header="DATA FRAMEWORK" :sortable="true" sortField="dataType" class="w-3">
        <template #body="{ data }">
          {{ humanizeString(data.dataType) }}
        </template>
      </Column>
      <Column v-if="isProperlyImplemented" field="year" header="YEAR" :sortable="true"></Column>
      <Column field="status" header="STATUS" :sortable="true" sortField="status.text">
        <template #body="{ data }">
          <span :class="`p-badge badge-${data.status.color} m-0`">{{ data.status.text }}</span>
        </template>
      </Column>
      <Column
        field="uploadTimeInSeconds"
        header="SUBMISSION DATE"
        :sortable="true"
        sortField="uploadTimeInSeconds"
        class="w-2"
      >
        <template #body="{ data }">
          <span>{{ convertDate(data.uploadTimeInSeconds) }}</span>
        </template>
      </Column>
      <Column field="companyName" header="" class="w-2 d-bg-white d-datatable-column-right">
        <template #header>
          <span class="w-12 p-input-icon-left">
            <i class="pi pi-search pl-3 pr-3" aria-hidden="true" style="color: #958d7c" />
            <InputText v-model="searchBarInput" placeholder="Search table" class="w-12 pl-6" />
          </span>
        </template>
        <template #body="{ data }">
          <router-link :to="getTableRowLinkTarget(data)" class="text-primary no-underline font-bold">
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
  padding-top: 0.8rem;
  padding-bottom: 0.8rem;
}
</style>

<script lang="ts">
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { humanizeString } from "@/utils/StringHumanizer";
import { DatasetStatus, DatasetTableInfo } from "@/components/resources/datasetOverview/DatasetTableInfo";
import InputText from "primevue/inputtext";
import { convertUnixTimeInMsToDateString } from "@/utils/DateFormatUtils";
import { DataTypeEnum } from "@clients/backend";

export default defineComponent({
  name: "DatasetOverviewTable",
  components: {
    DataTable,
    Column,
    InputText,
  },
  data() {
    return {
      searchBarInput: "",
      displayedDatasetTableInfos: [] as DatasetTableInfo[],
      humanizeString: humanizeString,
      convertDate: convertUnixTimeInMsToDateString,
    };
  },
  props: {
    datasetTableInfos: {
      type: Array,
      default: [],
    },
    isProperlyImplemented: {
      type: Boolean,
    },
  },
  watch: {
    searchBarInput() {
      this.applySearchFilter();
    },
    datasetTableInfos() {
      this.displayedDatasetTableInfos = this.datasetTableInfos as DatasetTableInfo[];
    },
  },
  methods: {
    /**
     * Computes the path a row click should lead to depending on the selected dataset and its status
     *
     * @param datasetTableInfo relevant dataset information
     * @returns the path depending on the status of the data set
     */
    getTableRowLinkTarget(datasetTableInfo: DatasetTableInfo): string {
      if (this.isDatasetRejected(datasetTableInfo)) {
        return `/companies/${datasetTableInfo.companyId}/frameworks/${datasetTableInfo.dataType}/upload`;
      } else {
        const dataTypesWithSingleView = [DataTypeEnum.Lksg] as DataTypeEnum[];
        let queryParameters = "";
        if (!dataTypesWithSingleView.includes(datasetTableInfo.dataType)) {
          queryParameters = `?dataId=${datasetTableInfo.dataId}`;
        }
        return `/companies/${datasetTableInfo.companyId}/frameworks/${datasetTableInfo.dataType}${queryParameters}`;
      }
    },
    /**
     * Checks if the status of th given DatasetTableInfo equals Rejected
     *
     * @param datasetTableEntry the DatasetTableEntry to check the status of
     * @returns a boolean reflecting if the data set is rejected or not
     */
    isDatasetRejected(datasetTableEntry: DatasetTableInfo): boolean {
      return datasetTableEntry.status.text === DatasetStatus.Requested.text;
    },
    /**
     * Filter the given datasets for the search string in the company name
     */
    applySearchFilter(): void {
      // TODO implement this properly
      this.displayedDatasetTableInfos = this.datasetTableInfos.filter((info) =>
        (info as DatasetTableInfo).companyName.toLowerCase().includes(this.searchBarInput.toLowerCase())
      ) as DatasetTableInfo[];
    },
    /**
     * Depending on the dataset status, executes a router push to either the dataset view page or an upload page
     *
     * @param event an event that stores the DatasetTableInfo of the on clicked row
     * @param event.data the DatasetTableInfo to be pushed to
     */
    rerouteRowClick(event: { data: DatasetTableInfo }) {
      void this.$router.push(this.getTableRowLinkTarget(event.data));
    },
  },
});
</script>
