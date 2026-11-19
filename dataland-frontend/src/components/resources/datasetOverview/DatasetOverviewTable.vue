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
      data-test="datasets-table"
    >
      <Column field="companyName" header="COMPANY" :sortable="true" class="w-3 data-test-company-name"></Column>
      <Column field="dataType" header="DATA FRAMEWORK" :sortable="true" sortField="dataType" class="w-3">
        <template #body="{ data }">
          {{ humanizeString(data.dataType) }}
        </template>
      </Column>
      <Column field="dataReportingPeriod" header="REPORTING PERIOD" :sortable="true"></Column>
      <Column field="status" header="STATUS" :sortable="true">
        <template #body="{ data }">
          <DatalandTag :severity="data.status" :value="data.status" data-test="qa-status" />
        </template>
      </Column>
      <Column field="uploadTimeInMs" header="SUBMISSION DATE" :sortable="true" sortField="uploadTimeInMs" class="w-2">
        <template #body="{ data }">
          <span>{{ convertDate(data.uploadTimeInMs) }}</span>
        </template>
      </Column>
      <Column field="companyName" header="" class="w-2 d-bg-white d-datatable-column-right">
        <template #header>
          <IconField class="w-12">
            <InputIcon class="pi pi-search" />
            <InputText v-model="searchBarInput" placeholder="Search table" fluid />
            <InputIcon v-if="loading" class="pi pi-spin pi-spinner" aria-hidden="true" />
          </IconField>
        </template>
        <template #body="{ data }">
          <div class="text-right">
            <PrimeButton
              label="VIEW"
              iconPos="right"
              icon="pi pi-angle-right"
              variant="link"
              data-test="view-dataset-button"
              @click="goToDataset(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DatasetTableInfo } from '@/components/resources/datasetOverview/DatasetTableInfo';
import InputText from 'primevue/inputtext';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import router from '@/router';
import DatalandTag from '@/components/general/DatalandTag.vue';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import PrimeButton from 'primevue/button';

const props = withDefaults(defineProps<{ datasetTableInfos?: DatasetTableInfo[] }>(), {
  datasetTableInfos: () => [],
});

const searchBarInput = ref('');
const displayedDatasetTableInfos = ref<DatasetTableInfo[]>([]);
const loading = ref(false);
const latestSearchString = ref('');

const humanizeString = humanizeStringOrNumber;
const convertDate = convertUnixTimeInMsToDateString;

/**
 * Computes the path a row click should lead to depending on the selected dataset and its status
 * @param datasetTableInfo relevant dataset information
 * @returns the path depending on the status of the data set
 */
function getTableRowLinkTarget(datasetTableInfo: DatasetTableInfo): string {
  return `/companies/${datasetTableInfo.companyId}/frameworks/${datasetTableInfo.dataType}/${datasetTableInfo.dataId}`;
}

/**
 * Navigates to the specified dataset's details page based on the provided dataset information.
 *
 * @param {DatasetTableInfo} datasetTableInfo - Object containing information about the dataset table to navigate to.
 * @return {void} Does not return a value.
 */
function goToDataset(datasetTableInfo: DatasetTableInfo): void {
  void router.push(getTableRowLinkTarget(datasetTableInfo));
}

/**
 * Filter the given datasets for the search string in the company name
 * @param searchString The search string to look for in the company names
 */
function applySearchFilter(searchString: string): void {
  loading.value = true;
  let arrayToFilter: DatasetTableInfo[];
  const lowerCaseSearchString = searchString.toLowerCase();
  if (lowerCaseSearchString.includes(latestSearchString.value.toLowerCase())) {
    arrayToFilter = displayedDatasetTableInfos.value;
  } else {
    arrayToFilter = props.datasetTableInfos;
  }
  displayedDatasetTableInfos.value = arrayToFilter.filter((datasetTableInfo: DatasetTableInfo) => {
    return datasetTableInfo.companyName.toLowerCase().includes(lowerCaseSearchString);
  });
  latestSearchString.value = lowerCaseSearchString;
  loading.value = false;
}

/**
 * Depending on the dataset status, executes a router push to either the dataset view page or an upload page
 * @param event an event that stores the DatasetTableInfo of the on clicked row
 * @param event.data the DatasetTableInfo to be pushed to
 */
function rerouteRowClick(event: { data: DatasetTableInfo }): void {
  void router.push(getTableRowLinkTarget(event.data));
}

watch(searchBarInput, (newSearchString) => {
  applySearchFilter(newSearchString);
});

watch(
  () => props.datasetTableInfos,
  () => {
    displayedDatasetTableInfos.value = props.datasetTableInfos;
  }
);

onMounted(() => {
  displayedDatasetTableInfos.value = props.datasetTableInfos;
});
</script>

<style>
#dataset-overview-table tr:hover {
  cursor: pointer;
}

#dataset-overview-table th {
  padding-top: 0.8rem;
  padding-bottom: 0.8rem;
}
</style>

<style scoped>
.text-primary {
  color: var(--main-color);
}
</style>
