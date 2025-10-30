<template>
  <TheContent class="min-h-screen relative">
    <div v-if="waitingForData || storedDataRequests.length > 0">
      <div class="container">
        <IconField class="company-search">
          <InputIcon class="pi pi-search" />
          <InputText
            data-test="requested-datasets-searchbar"
            v-model="searchBarInput"
            placeholder="Search by Company Name"
            fluid
            variant="filled"
          />
        </IconField>

        <FrameworkDataSearchDropdownFilter
          v-model="selectedFrameworks"
          ref="frameworkFilter"
          :available-items="availableFrameworks"
          filter-name="Framework"
          data-test="requested-datasets-frameworks"
          filter-placeholder="Search Frameworks"
          class="search-filter"
          :max-selected-labels="1"
          selected-items-label="{0} Frameworks selected"
        />

        <FrameworkDataSearchDropdownFilter
          v-model="selectedState"
          ref="stateFilter"
          :available-items="availableState"
          filter-name="Request State"
          data-test="requested-datasets-state"
          filter-placeholder="Search State"
          class="search-filter"
          :max-selected-labels="1"
          selected-items-label="{0} States selected"
        />
        <PrimeButton variant="text" @click="resetFilterAndSearchBar" label="RESET" data-test="reset-filter" />
      </div>

      <div style="padding: var(--spacing-md)">
        <DataTable
          :value="displayedData"
          style="cursor: pointer"
          :row-hover="true"
          :loading="waitingForData"
          data-test="requested-datasets-table"
          paginator
          paginator-position="both"
          :rows="datasetsPerPage"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
          :alwaysShowPaginator="true"
          currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
          lazy
          :total-records="numberOfFilteredRequests"
          @page="onPage"
          @sort="onSort"
          @row-click="onRowClick"
        >
          <Column header="COMPANY" field="companyName" :sortable="true" />
          <Column header="FRAMEWORK" field="dataType" :sortable="true">
            <template #body="{ data }">
              <div>{{ getFrameworkTitle(data.dataType) }}</div>
              <div
                v-if="frameworkHasSubTitle(data.dataType)"
                data-test="framework-subtitle"
                style="color: gray; font-size: var(--font-size-xs); line-height: 0.5; white-space: nowrap"
              >
                <br />
                {{ getFrameworkSubtitle(data.dataType) }}
              </div>
            </template>
          </Column>
          <Column header="REPORTING PERIOD" field="reportingPeriod" :sortable="true" />
          <Column header="REQUESTED" field="creationTimeStamp" :sortable="true">
            <template #body="{ data }">
              {{ convertUnixTimeInMsToDateString(data.creationTimeStamp) }}
            </template>
          </Column>
          <Column header="LAST UPDATED" field="lastModifiedDate" :sortable="true">
            <template #body="{ data }">
              {{ convertUnixTimeInMsToDateString(data.lastModifiedDate) }}
            </template>
          </Column>
          <Column header="REQUEST STATE" field="state" :sortable="true">
            <template #body="{ data }">
              <DatalandTag :severity="data.state" :value="data.state" />
            </template>
          </Column>
          <Column field="resolve" header="">
            <template #body="{ data }">
              <div v-if="data.state === RequestState.Processed" class="text-primary no-underline">
                <span id="resolveButton" style="cursor: pointer" data-test="requested-datasets-resolve">RESOLVE</span>
                <span style="margin: var(--spacing-md)">&gt;</span>
              </div>
            </template>
          </Column>
          <template #empty>
            <div style="text-align: center; font-weight: var(--font-weight-bold)">No requests found.</div>
          </template>
        </DataTable>
      </div>
    </div>

    <div v-if="!waitingForData && storedDataRequests.length === 0">
      <div class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">You have not requested data yet.</p>
        <p class="font-medium text-xl">Individual data requests can be made for each company from its cockpit page.</p>
        <p class="font-medium text-xl">
          Alternatively, become a premium user and create a portfolio for automatic request creation.
        </p>
        <PrimeButton
          label="MANAGE YOUR PORTFOLIOS"
          icon="pi pi-plus-circle"
          data-test="myPortfoliosButton"
          @click="goToMyPortfoliosPage"
        />
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import DatalandTag from '@/components/general/DatalandTag.vue';
import TheContent from '@/components/generics/TheContent.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';

import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { type FrameworkSelectableItem, type SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import {
  customCompareForRequestState,
  retrieveAvailableFrameworks,
  retrieveAvailableRequestStates,
} from '@/utils/RequestsOverviewPageUtils';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import Column from 'primevue/column';
import DataTable, {
  type DataTablePageEvent,
  type DataTableRowClickEvent,
  type DataTableSortEvent,
} from 'primevue/datatable';
import InputText from 'primevue/inputtext';
import { inject, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { type ExtendedStoredRequest, RequestState } from '@clients/datasourcingservice';

const datasetsPerPage = 100;

const waitingForData = ref(true);
const currentPage = ref(0);
const storedDataRequests = ref<ExtendedStoredRequest[]>([]);
const displayedData = ref<ExtendedStoredRequest[]>([]);
const searchBarInput = ref('');
const searchBarInputFilter = ref('');

const availableFrameworks = ref<FrameworkSelectableItem[]>([]);
const selectedFrameworks = ref<FrameworkSelectableItem[]>([]);

const availableState = ref<SelectableItem[]>([]);
const selectedState = ref<SelectableItem[]>([]);

const numberOfFilteredRequests = ref(0);
const sortField = ref<keyof ExtendedStoredRequest>('state');
const sortOrder = ref(1);

const frameworkFilter = ref();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const vueRouter = useRouter();

onMounted(async () => {
  availableFrameworks.value = retrieveAvailableFrameworks();
  availableState.value = retrieveAvailableRequestStates();
  await getStoredRequestDataList();
});

watch([selectedFrameworks, selectedState, waitingForData], () => updateCurrentDisplayedData(), { deep: true });

watch(searchBarInput, (newSearch) => {
  searchBarInputFilter.value = newSearch;
  updateCurrentDisplayedData();
});

/**
 * Navigates to the bulk data request page.
 */
function goToMyPortfoliosPage(): void {
  void vueRouter.push('/portfolios');
}

/**
 * Retrieves and updates the list of stored data requests for the requesting user.
 * Utilizes Keycloak authentication and the API client for fetching the data.
 */
async function getStoredRequestDataList(): Promise<void> {
  waitingForData.value = true;
  storedDataRequests.value.length = 0;
  try {
    if (getKeycloakPromise) {
      storedDataRequests.value = (
        await new ApiClientProvider(getKeycloakPromise()).apiClients.requestController.getRequestsForRequestingUser()
      ).data;
    }
  } catch (error) {
    console.error(error);
  }
  waitingForData.value = false;
}

/**
 * Handles the click event on a row in the DataTable.
 * Redirects the user to the details page for the clicked request.
 *
 * @param {DataTableRowClickEvent} event - The row click event containing data about the clicked row.
 */
function onRowClick(event: DataTableRowClickEvent): void {
  const requestIdOfClickedRow = (event.data as ExtendedStoredRequest).id;
  void vueRouter.push(`/requests/${requestIdOfClickedRow}`);
}

/**
 * Handles the sorting of data in the DataTable component.
 * Updates the sort field and order based on the event properties,
 * and triggers the update of the currently displayed data.
 *
 * @param {DataTableSortEvent} event - The sorting event containing the sort field and sort order.
 */
function onSort(event: DataTableSortEvent): void {
  sortField.value = event.sortField as keyof ExtendedStoredRequest;
  sortOrder.value = event.sortOrder ?? 1;
  updateCurrentDisplayedData();
}

/**
 * Filters the selected frameworks to determine if a given framework matches any selection.
 *
 * @param {string} framework - The framework data type to check against the selected frameworks.
 * @returns {boolean} True if the given framework matches a selected framework, false otherwise.
 */
function filterFramework(framework: string): boolean {
  return selectedFrameworks.value.some((f) => f.frameworkDataType === framework);
}

/**
 * Determines whether the specified state matches any selected state.
 *
 * @param {string} state - The state to check.
 * @returns {boolean} True if the specified state matches a selected state, false otherwise.
 */
function filterState(state: string): boolean {
  return selectedState.value.some((s) => s.displayName === state);
}

/**
 * Filters company names based on the search bar input.
 *
 * @param {string} companyName - The name of the company to be checked against the search input.
 * @returns {boolean} True if the company name matches the search input, false otherwise.
 */
function filterSearchInput(companyName: string): boolean {
  return (companyName ?? '').toLowerCase().includes(searchBarInputFilter.value.toLowerCase());
}

/**
 * Resets all the filters and the search bar input to their default state.
 * This clears the selected frameworks, selected states, and search input values.
 */
function resetFilterAndSearchBar(): void {
  selectedFrameworks.value = [];
  selectedState.value = [];
  searchBarInput.value = '';
}

/**
 * Updates the list of currently displayed data based on filters, sorting, and pagination.
 * Filters the data requests by search input, selected frameworks, and states.
 * Sorts the filtered data using a custom comparison function.
 * Updates the displayed data and scrolls to the top of the page.
 */
function updateCurrentDisplayedData(): void {
  let data = storedDataRequests.value.filter((request) => filterSearchInput(request.companyName));

  if (selectedFrameworks.value.length > 0) {
    data = data.filter((request) => filterFramework(request.dataType));
  }
  if (selectedState.value.length > 0) {
    data = data.filter((request) => filterState(request.state));
  }

  data.sort((dataRequestObjectA, dataRequestObjectB) =>
    customCompareForExtendedStoredDataRequests(dataRequestObjectA, dataRequestObjectB)
  );

  numberOfFilteredRequests.value = data.length;

  displayedData.value = data.slice(datasetsPerPage * currentPage.value, datasetsPerPage * (currentPage.value + 1));

  globalThis.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Custom comparison function for sorting `ExtendedStoredDataRequest` objects.
 * Compares based on the current sort field, request state, last modified date, and company name.
 *
 * @param {ExtendedStoredRequest} dataRequestObjectA - The first data request object to compare.
 * @param {ExtendedStoredRequest} dataRequestObjectB - The second data request object to compare.
 * @returns {number} Comparison result: negative if `dataRequestObjectA` should precede `dataRequestObjectB`, positive if `dataRequestObjectB` should precede `dataRequestObjectA`, or zero if they are equal.
 */
function customCompareForExtendedStoredDataRequests(
  dataRequestObjectA: ExtendedStoredRequest,
  dataRequestObjectB: ExtendedStoredRequest
): number {
  const dataRequestObjectValueA = dataRequestObjectA[sortField.value] ?? '';
  const dataRequestObjectValueB = dataRequestObjectB[sortField.value] ?? '';

  if (sortField.value !== 'state') {
    if (dataRequestObjectValueA < dataRequestObjectValueB) return -1 * sortOrder.value;
    if (dataRequestObjectValueA > dataRequestObjectValueB) return sortOrder.value;
  }

  if (dataRequestObjectA.state !== dataRequestObjectB.state)
    return customCompareForRequestState(dataRequestObjectA.state, dataRequestObjectB.state, sortOrder.value);

  if (dataRequestObjectA.lastModifiedDate < dataRequestObjectB.lastModifiedDate) return sortOrder.value;
  if (dataRequestObjectA.lastModifiedDate > dataRequestObjectB.lastModifiedDate) return -1 * sortOrder.value;

  return dataRequestObjectA.companyName < dataRequestObjectB.companyName ? -1 * sortOrder.value : sortOrder.value;
}

/**
 * Handles the pagination event in the DataTable component.
 * Updates the current page based on the event properties and refreshes the displayed data.
 *
 * @param {DataTablePageEvent} event - The pagination event containing information about the current page.
 */
function onPage(event: DataTablePageEvent): void {
  currentPage.value = event.page;
  updateCurrentDisplayedData();
}
</script>

<style scoped>
.container {
  padding: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-lg);

  .company-search {
    width: 30%;
  }

  .search-filter {
    width: 13%;
    text-align: left;
  }
}
</style>
