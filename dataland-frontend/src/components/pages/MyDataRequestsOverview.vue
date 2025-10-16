<template>
  <TheContent class="min-h-screen relative">
    <div v-if="waitingForData || storedDataRequests.length > 0">
      <div class="container">
        <IconField id="company-search-bar" class="company-search">
          <InputIcon class="pi pi-search" />
          <InputText
            data-test="requested-datasets-searchbar"
            v-model="searchBarInput"
            placeholder="Search by company name"
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
          id="framework-filter"
          filter-placeholder="Search frameworks"
          class="search-filter"
          :max-selected-labels="1"
          selected-items-label="{0} frameworks selected"
        />

        <FrameworkDataSearchDropdownFilter
          v-model="selectedAccessStatus"
          ref="accessStatusFilter"
          :available-items="availableAccessStatus"
          filter-name="Access Status"
          data-test="requested-datasets-access-status"
          id="access-status-filter"
          filter-placeholder="access status"
          class="search-filter"
          :max-selected-labels="1"
          selected-items-label="{0} status selected"
        />
        <PrimeButton variant="link" @click="resetFilterAndSearchBar" label="RESET" data-test="reset-filter" />
      </div>

      <div class="col-12 text-left p-3">
        <div class="card">
          <DataTable
            :value="displayedData"
            style="cursor: pointer"
            :row-hover="true"
            :loading="waitingForData"
            data-test="requested-datasets-table"
            paginator
            paginator-position="bottom"
            :rows="datasetsPerPage"
            lazy
            :total-records="numberOfFilteredRequests"
            @page="onPage"
            @sort="onSort"
            @row-click="onRowClick"
            id="my-data-requests-overview-table"
          >
            <Column header="COMPANY" field="companyName" :sortable="true">
              <template #body="{ data }">{{ data.companyName }}</template>
            </Column>
            <Column header="FRAMEWORK" field="dataType" :sortable="true">
              <template #body="{ data }">
                <div>{{ getFrameworkTitle(data.dataType) }}</div>
                <div
                  v-if="frameworkHasSubTitle(data.dataType)"
                  data-test="framework-subtitle"
                  style="color: gray; font-size: smaller; line-height: 0.5; white-space: nowrap"
                >
                  <br />
                  {{ getFrameworkSubtitle(data.dataType) }}
                </div>
              </template>
            </Column>
            <Column header="REPORTING PERIOD" field="reportingPeriod" :sortable="true">
              <template #body="{ data }">{{ data.reportingPeriod }}</template>
            </Column>
            <Column header="REQUESTED" field="creationTimestamp" :sortable="true">
              <template #body="{ data }">
                {{ convertUnixTimeInMsToDateString(data.creationTimestamp) }}
              </template>
            </Column>
            <Column header="LAST UPDATED" field="lastModifiedDate" :sortable="true">
              <template #body="{ data }">
                {{ convertUnixTimeInMsToDateString(data.lastModifiedDate) }}
              </template>
            </Column>
            <Column header="REQUEST STATUS" field="requestStatus" :sortable="true">
              <template #body="{ data }">
                <DatalandTag :severity="data.state" :value="data.state" />
              </template>
            </Column>
            <Column field="resolve" header="">
              <template #body="{ data }">
                <div
                  v-if="data.requestStatus === RequestState.Processed"
                  class="text-right text-primary no-underline font-bold"
                >
                  <span id="resolveButton" style="cursor: pointer" data-test="requested-Datasets-Resolve">RESOLVE</span>
                  <span class="ml-3">&gt;</span>
                </div>
              </template>
            </Column>
          </DataTable>
        </div>
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
  customCompareForRequestStatus,
  retrieveAvailableAccessStatuses,
  retrieveAvailableFrameworks,
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
import {ExtendedStoredRequest, RequestState} from "@clients/datasourcingservice";

const datasetsPerPage = 100;

const waitingForData = ref(true);
const currentPage = ref(0);
const storedDataRequests = ref<ExtendedStoredRequest[]>([]);
const displayedData = ref<ExtendedStoredRequest[]>([]);
const searchBarInput = ref('');
const searchBarInputFilter = ref('');

const availableFrameworks = ref<FrameworkSelectableItem[]>([]);
const selectedFrameworks = ref<FrameworkSelectableItem[]>([]);

const availableAccessStatus = ref<SelectableItem[]>([]);
const selectedAccessStatus = ref<SelectableItem[]>([]);

const numberOfFilteredRequests = ref(0);
const sortField = ref<keyof ExtendedStoredRequest>('state');
const sortOrder = ref(1);

const frameworkFilter = ref();
const accessStatusFilter = ref();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const vueRouter = useRouter();

onMounted(async () => {
  availableFrameworks.value = retrieveAvailableFrameworks();
  availableAccessStatus.value = retrieveAvailableAccessStatuses();
  await getStoredRequestDataList();
});

watch([selectedFrameworks, selectedAccessStatus, waitingForData], () => updateCurrentDisplayedData(), { deep: true });

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
        await new ApiClientProvider(
          getKeycloakPromise()
        ).apiClients.requestController.getRequestsForRequestingUser()
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
 * Determines whether the specified access status matches any selected access status.
 *
 * @param {string} accessStatus - The access status to check.
 * @returns {boolean} True if the specified access status matches a selected access status, false otherwise.
 */
function filterAccessStatus(accessStatus: string): boolean {
  return selectedAccessStatus.value.some((s) => s.displayName === accessStatus);
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
 * This clears the selected frameworks, selected access statuses, and search input values.
 */
function resetFilterAndSearchBar(): void {
  selectedFrameworks.value = [];
  selectedAccessStatus.value = [];
  searchBarInput.value = '';
}

/**
 * Updates the list of currently displayed data based on filters, sorting, and pagination.
 * Filters the data requests by search input, selected frameworks, and access statuses.
 * Sorts the filtered data using a custom comparison function.
 * Updates the displayed data and scrolls to the top of the page.
 */
function updateCurrentDisplayedData(): void {
  let data = storedDataRequests.value.filter((d) => filterSearchInput(d.companyName!));

  if (selectedFrameworks.value.length > 0) {
    data = data.filter((d) => filterFramework(d.dataType));
  }
  if (selectedAccessStatus.value.length > 0) {
    data = data.filter((d) => filterAccessStatus(d.state));
  }

  data.sort((a, b) => customCompareForExtendedStoredDataRequests(a, b));

  numberOfFilteredRequests.value = data.length;

  displayedData.value = data.slice(datasetsPerPage * currentPage.value, datasetsPerPage * (currentPage.value + 1));

  globalThis.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Custom comparison function for sorting `ExtendedStoredDataRequest` objects.
 * Compares based on the current sort field, request status, last modified date, and company name.
 *
 * @param {ExtendedStoredRequest} a - The first data request object to compare.
 * @param {ExtendedStoredRequest} b - The second data request object to compare.
 * @returns {number} Comparison result: negative if `a` should precede `b`, positive if `b` should precede `a`, or zero if they are equal.
 */
function customCompareForExtendedStoredDataRequests(
  a: ExtendedStoredRequest,
  b: ExtendedStoredRequest
):
    number {
  const aValue = a[sortField.value] ?? '';
  const bValue = b[sortField.value] ?? '';

  if (sortField.value !== 'state') {
    if (aValue < bValue) return -1 * sortOrder.value;
    if (aValue > bValue) return sortOrder.value;
  }

  if (a.state !== b.state)
    return customCompareForRequestStatus(a.state, b.state, sortOrder.value);

  if (a.lastModifiedDate < b.lastModifiedDate) return sortOrder.value;
  if (a.lastModifiedDate > b.lastModifiedDate) return -1 * sortOrder.value;

  return a.companyName! < b.companyName! ? -1 * sortOrder.value : sortOrder.value;
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
  margin: 0;
  width: 100%;
  padding: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-lg);
  align-items: start;

  .company-search {
    width: 30%;
  }

  .search-filter {
    width: 13%;
    text-align: left;
  }
}

#my-data-requests-overview-table tr:hover {
  cursor: pointer;
}

.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}

.text-primary {
  color: var(--main-color);
}
</style>
