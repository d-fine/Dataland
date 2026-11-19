<template>
  <TheContent class="min-h-screen relative">
    <div v-if="isError && !storedDataRequests" class="d-center-div text-center px-7 py-4" data-test="requests-error">
      <p class="font-medium text-xl">Your data requests could not be loaded.</p>
      <p class="font-medium text-xl">Please try again later or contact the support team if the issue persists.</p>
    </div>
    <div v-else-if="isPending || hasStoredDataRequests">
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
          :available-items="availableState"
          filter-name="State"
          data-test="requested-datasets-state"
          filter-placeholder="Search State"
          class="search-filter"
          :max-selected-labels="1"
          selected-items-label="{0} States selected"
        />
        <PrimeButton variant="text" @click="resetFilterAndSearchBar" label="RESET" data-test="reset-filter" />
      </div>
      <div v-if="isPending">
        <p class="font-medium text-xl">Loading...</p>
        <DatalandProgressSpinner />
      </div>

      <div v-else style="padding: var(--spacing-md)">
        <DataTable
          :value="displayedData"
          style="cursor: pointer"
          :row-hover="true"
          data-test="requested-datasets-table"
          :first="currentPage * datasetsPerPage"
          paginator
          paginator-position="both"
          :rows="datasetsPerPage"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
          :alwaysShowPaginator="true"
          currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
          lazy
          :total-records="filteredAndSortedRequests.length"
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
          <Column header="STATE" field="state" :sortable="true">
            <template #body="{ data }">
              <DatalandTag
                :severity="getDisplayedState(data)"
                :value="getDisplayedStateLabel(getDisplayedState(data))"
              />
            </template>
          </Column>
          <Column header="NEXT DOCUMENT SOURCING ATTEMPT" field="nextDataSourcingAttempt" :sortable="true">
            <template #body="{ data }">
              {{ dateStringFormatter(data.dataSourcingDetails?.dateOfNextDocumentSourcingAttempt) || '-' }}
            </template>
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
          <template #empty>
            <div style="text-align: center; font-weight: var(--font-weight-bold)">No requests found.</div>
          </template>
        </DataTable>
      </div>
    </div>

    <div v-else>
      <div class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">You have not requested data yet.</p>
        <p class="font-medium text-xl">Individual data requests can be made for each company from its cockpit page.</p>
        <p class="font-medium text-xl">
          Alternatively, become a Dataland member and create a portfolio for automatic request creation.
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
import { useGetRequestsForRequestingUserQuery } from '@/api-queries/data-sourcing/enhanced-request/useGetRequestsForRequestingUserQuery';
import { convertUnixTimeInMsToDateString, dateStringFormatter } from '@/utils/DataFormatUtils';
import { type FrameworkSelectableItem, type SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import {
  customCompareForState,
  retrieveAvailableDataSourcingStates,
  retrieveAvailableFrameworks,
  getDisplayedState,
  getDisplayedStateLabel,
} from '@/utils/RequestsOverviewPageUtils';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
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
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { type DataSourcingEnhancedRequest, RequestState } from '@clients/datasourcingservice';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';

const datasetsPerPage = 100;

const availableFrameworks: FrameworkSelectableItem[] = retrieveAvailableFrameworks();
const availableState: SelectableItem[] = [
  { displayName: RequestState.Open, disabled: false },
  ...retrieveAvailableDataSourcingStates(),
  { displayName: RequestState.Withdrawn, disabled: false },
];

const { data: storedDataRequests, isPending, isError } = useGetRequestsForRequestingUserQuery();

const currentPage = ref(0);
const searchBarInput = ref('');
const selectedFrameworks = ref<FrameworkSelectableItem[]>([]);
const selectedState = ref<SelectableItem[]>([]);
const sortField = ref<keyof DataSourcingEnhancedRequest>('state');
const sortOrder = ref(1);

const vueRouter = useRouter();

const hasStoredDataRequests = computed(() => (storedDataRequests.value?.length ?? 0) > 0);

const filteredAndSortedRequests = computed(() =>
  (storedDataRequests.value ?? [])
    .filter((request) => filterSearchInput(request.companyName))
    .filter((request) => selectedFrameworks.value.length === 0 || filterFramework(request.dataType))
    .filter(
      (request) => selectedState.value.length === 0 || filterState(getDisplayedStateLabel(getDisplayedState(request)))
    )
    .sort(customCompareForExtendedStoredDataRequests)
);

const displayedData = computed(() =>
  filteredAndSortedRequests.value.slice(datasetsPerPage * currentPage.value, datasetsPerPage * (currentPage.value + 1))
);

watch([selectedFrameworks, selectedState, searchBarInput], () => {
  currentPage.value = 0;
  scrollToTop();
});

/**
 * Navigates to the bulk data request page.
 */
function goToMyPortfoliosPage(): void {
  void vueRouter.push('/portfolios');
}

/**
 * Scrolls the page to the top, e.g. after the displayed rows changed.
 */
function scrollToTop(): void {
  globalThis.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Handles the click event on a row in the DataTable.
 * Redirects the user to the details page for the clicked request.
 *
 * @param {DataTableRowClickEvent} event - The row click event containing data about the clicked row.
 */
function onRowClick(event: DataTableRowClickEvent): void {
  const requestIdOfClickedRow = (event.data as DataSourcingEnhancedRequest).id;
  void vueRouter.push(`/requests/${requestIdOfClickedRow}`);
}

/**
 * Handles the sorting of data in the DataTable component.
 * Updates the sort field and order based on the event properties.
 *
 * @param {DataTableSortEvent} event - The sorting event containing the sort field and sort order.
 */
function onSort(event: DataTableSortEvent): void {
  sortField.value = event.sortField as keyof DataSourcingEnhancedRequest;
  sortOrder.value = event.sortOrder ?? 1;
  scrollToTop();
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
  return (companyName ?? '').toLowerCase().includes(searchBarInput.value.toLowerCase());
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
 * Custom comparison function for sorting `ExtendedStoredDataRequest` objects.
 * Compares based on the current sort field, request state, last modified date, and company name.
 *
 * @param {DataSourcingEnhancedRequest} dataRequestObjectA - The first data request object to compare.
 * @param {DataSourcingEnhancedRequest} dataRequestObjectB - The second data request object to compare.
 * @returns {number} Comparison result: negative if `dataRequestObjectA` should precede `dataRequestObjectB`, positive if `dataRequestObjectB` should precede `dataRequestObjectA`, or zero if they are equal.
 */
function customCompareForExtendedStoredDataRequests(
  dataRequestObjectA: DataSourcingEnhancedRequest,
  dataRequestObjectB: DataSourcingEnhancedRequest
): number {
  const dataRequestObjectValueA = dataRequestObjectA[sortField.value] ?? '';
  const dataRequestObjectValueB = dataRequestObjectB[sortField.value] ?? '';

  if (sortField.value !== 'state') {
    if (dataRequestObjectValueA < dataRequestObjectValueB) return -1 * sortOrder.value;
    if (dataRequestObjectValueA > dataRequestObjectValueB) return sortOrder.value;
  }

  const stateA = getDisplayedState(dataRequestObjectA);
  const stateB = getDisplayedState(dataRequestObjectB);

  if (stateA !== stateB) {
    return customCompareForState(stateA, stateB, sortOrder.value);
  }

  if (dataRequestObjectA.lastModifiedDate < dataRequestObjectB.lastModifiedDate) return sortOrder.value;
  if (dataRequestObjectA.lastModifiedDate > dataRequestObjectB.lastModifiedDate) return -1 * sortOrder.value;

  return dataRequestObjectA.companyName < dataRequestObjectB.companyName ? -1 * sortOrder.value : sortOrder.value;
}

/**
 * Handles the pagination event in the DataTable component.
 * Updates the current page based on the event properties.
 *
 * @param {DataTablePageEvent} event - The pagination event containing information about the current page.
 */
function onPage(event: DataTablePageEvent): void {
  currentPage.value = event.page;
  scrollToTop();
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
