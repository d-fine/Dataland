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
      sortField="uploadTimeInMs"
      :sortOrder="-1"
    >
      <Column field="companyName" header="COMPANY" :sortable="true" class="w-3"></Column>
      <Column field="dataType" header="DATA FRAMEWORK" :sortable="true" sortField="dataType" class="w-3">
        <template #body="{ data }">
          {{ humanizeString(data.dataType) }}
        </template>
      </Column>
      <Column field="dataReportingPeriod" header="REPORTING PERIOD" :sortable="true"></Column>
      <Column field="status" header="STATUS" :sortable="true">
        <template #body="{ data }">
          <DatasetStatusBadge :dataset-status="data.status" />
        </template>
      </Column>
      <Column field="uploadTimeInMs" header="SUBMISSION DATE" :sortable="true" sortField="uploadTimeInMs" class="w-2">
        <template #body="{ data }">
          <span>{{ convertDate(data.uploadTimeInMs) }}</span>
        </template>
      </Column>
      <Column field="companyName" header="" class="w-2 d-bg-white d-datatable-column-right">
        <template #header>
          <span class="w-12 p-input-icon-left p-input-icon-right">
            <i class="pi pi-search pl-3 pr-3" aria-hidden="true" style="color: #958d7c" />
            <InputText v-model="searchBarInput" placeholder="Search table" class="w-12 pl-6 pr-6" />
            <i v-if="loading" class="pi pi-spin pi-spinner right-0 mr-3" aria-hidden="true"></i>
          </span>
        </template>
        <template #body="{ data }">
          <router-link :to="getTableRowLinkTarget(data)" class="text-primary no-underline font-bold">
            <div class="text-right">
              <span>VIEW</span>
              <span class="ml-3">></span>
            </div>
          </router-link>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

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
import { defineComponent, inject } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { humanizeString } from "@/utils/StringHumanizer";
import { DatasetTableInfo } from "@/components/resources/datasetOverview/DatasetTableInfo";
import InputText from "primevue/inputtext";
import { convertUnixTimeInMsToDateString } from "@/utils/DateFormatUtils";
import Keycloak from "keycloak-js";
import DatasetStatusBadge from "@/components/general/DatasetStatusBadge.vue";

export default defineComponent({
  name: "DatasetOverviewTable",
  mounted() {
    this.displayedDatasetTableInfos = this.datasetTableInfos as DatasetTableInfo[];
  },
  components: {
    DatasetStatusBadge,
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
      loading: false,
      latestSearchString: "" as string,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  props: {
    datasetTableInfos: {
      type: Array,
      default: [] as DatasetTableInfo[],
    },
  },
  watch: {
    searchBarInput(newSearchString) {
      this.applySearchFilter(newSearchString as string);
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
      return `/companies/${datasetTableInfo.companyId}/frameworks/${datasetTableInfo.dataType}/${datasetTableInfo.dataId}`;
    },

    /**
     * Filter the given datasets for the search string in the company name
     *
     * @param searchString The search string to look for in the company names
     */
    applySearchFilter(searchString: string): void {
      this.loading = true;
      let arrayToFilter: DatasetTableInfo[];
      if (searchString.includes(this.latestSearchString)) {
        arrayToFilter = this.displayedDatasetTableInfos;
      } else {
        arrayToFilter = this.datasetTableInfos as DatasetTableInfo[];
      }
      this.displayedDatasetTableInfos = arrayToFilter.filter((datasetTableInfo: DatasetTableInfo) => {
        return datasetTableInfo.companyName.includes(searchString);
      });
      this.latestSearchString = searchString;
      this.loading = false;
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
