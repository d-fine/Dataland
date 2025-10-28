<template>
  <TheContent>
    <div class="search-container-first-line">
      <IconField class="search-bar">
        <InputIcon class="pi pi-search" />
        <InputText
          data-test="company-search-string-searchbar"
          v-model="searchBarInputCompanySearchString"
          placeholder="Search by Company Name or Identifier"
          fluid
          variant="filled"
          :disabled="waitingForData"
        />
      </IconField>
      <IconField class="search-bar">
        <InputIcon class="pi pi-search" />
        <InputText
          data-test="email-searchbar"
          v-model="searchBarInputEmail"
          placeholder="Search by Requester"
          fluid
          variant="filled"
          :disabled="waitingForData"
        />
      </IconField>
      <IconField class="search-bar">
        <InputIcon class="pi pi-search" />
        <InputText
          data-test="comment-searchbar"
          v-model="searchBarInputComment"
          placeholder="Search by Comment"
          fluid
          variant="filled"
          :disabled="waitingForData"
        />
      </IconField>
    </div>
    <div class="search-container-last-line">
      <DataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedFrameworks"
        ref="frameworkFilter"
        :available-items="availableFrameworks"
        filter-name="Framework"
        data-test="framework-picker"
        filter-placeholder="Search by Frameworks"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} frameworks"
      />
      <DataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedRequestStates"
        ref="frameworkFilter"
        :available-items="availableRequestStates"
        filter-name="Request State"
        data-test="request-status-picker"
        filter-placeholder="Search by Request State"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} request states"
      />
      <DataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedPriorities"
        ref="frameworkFilter"
        :available-items="availablePriorities"
        filter-name="Priority"
        data-test="request-priority-picker"
        filter-placeholder="Search by Priority"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} request priorities"
      />
      <DataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedReportingPeriods"
        ref="frameworkFilter"
        :available-items="availableReportingPeriods"
        filter-name="Reporting Period"
        data-test="reporting-period-picker"
        filter-placeholder="Search by Reporting Period"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} reporting periods"
      />
      <PrimeButton variant="text" @click="resetFilterAndSearchBar" label="RESET" data-test="reset-filter" />
      <PrimeButton
        :disabled="waitingForData"
        data-test="trigger-filtering-requests"
        @click="getAllRequestsForFilters"
        label="FILTER REQUESTS"
      />
    </div>
    <div style="display: flex; justify-content: flex-end; padding-right: var(--spacing-xl)">
      <Message variant="simple" severity="secondary">{{ numberOfRequestsInformation }} </Message>
    </div>

    <div v-if="waitingForData">
      <p class="font-medium text-xl">Loading...</p>
      <DatalandProgressSpinner />
    </div>

    <div style="padding: var(--spacing-md)">
      <div class="card">
        <DataTable
          v-if="currentDataRequests && currentDataRequests.length > 0"
          v-show="!waitingForData"
          ref="dataTable"
          data-test="requests-datatable"
          :value="currentDataRequests"
          :paginator="true"
          :lazy="true"
          :total-records="totalRecords"
          :rows="rowsPerPage"
          :first="firstRowIndex"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
          :alwaysShowPaginator="false"
          currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
          @row-click="onRowClick($event)"
          @page="onPage($event)"
          class="table-cursor"
          id="admin-request-overview-data"
          :rowHover="true"
          style="cursor: pointer"
        >
          <Column header="REQUESTER" field="userEmailAddress" :sortable="false">
            <template #body="slotProps">
              {{ slotProps.data.userEmailAddress }}
            </template>
          </Column>
          <Column header="COMPANY" field="companyName" :sortable="false">
            <template #body="slotProps">
              {{ slotProps.data.companyName }}
            </template>
          </Column>
          <Column header="FRAMEWORK" :sortable="false" field="dataType">
            <template #body="slotProps">
              <div>
                {{ getFrameworkTitle(slotProps.data.dataType) }}
              </div>
              <div
                data-test="framework-subtitle"
                v-if="frameworkHasSubTitle(slotProps.data.dataType)"
                style="color: gray; font-size: smaller; line-height: var(--spacing-xs); white-space: nowrap"
              >
                <br />
                {{ getFrameworkSubtitle(slotProps.data.dataType) }}
              </div>
            </template>
          </Column>
          <Column header="REPORTING PERIOD" field="reportingPeriod" :sortable="false">
            <template #body="slotProps">
              {{ slotProps.data.reportingPeriod }}
            </template>
          </Column>
          <Column header="REQUEST ID" field="dataRequestId" :sortable="false">
            <template #body="slotProps">
              {{ slotProps.data.id }}
            </template>
          </Column>
          <Column header="REQUESTED" field="creationTimestamp" :sortable="false">
            <template #body="slotProps">
              <div>
                {{ convertUnixTimeInMsToDateString(slotProps.data.creationTimeStamp) }}
              </div>
            </template>
          </Column>
          <Column header="LAST UPDATED" :sortable="false" field="lastModifiedDate">
            <template #body="slotProps">
              <div>
                {{ convertUnixTimeInMsToDateString(slotProps.data.lastModifiedDate) }}
              </div>
            </template>
          </Column>
          <Column header="REQUEST STATE" :sortable="false" field="requestState">
            <template #body="slotProps">
              <DatalandTag :severity="slotProps.data.state" :value="slotProps.data.state" rounded />
            </template>
          </Column>
          <Column header="REQUEST PRIORITY" :sortable="false" field="priority">
            <template #body="slotProps">
              <DatalandTag :severity="slotProps.data.requestPriority" :value="slotProps.data.requestPriority" />
            </template>
          </Column>
          <Column header="ADMIN COMMENT" :sortable="false" field="adminComment">
            <template #body="slotProps">
              <div>
                {{ slotProps.data.adminComment }}
              </div>
            </template>
          </Column>
        </DataTable>
        <div v-if="!waitingForData && currentDataRequests.length == 0">
          <div style="text-align: center">
            <h2>There are no data requests on Dataland matching your filters.</h2>
          </div>
        </div>
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, inject } from 'vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatalandTag from '@/components/general/DatalandTag.vue';
import TheContent from '@/components/generics/TheContent.vue';
import DataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/DataSearchDropdownFilter.vue';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import type { FrameworkSelectableItem, SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import {
  retrieveAvailableFrameworks,
  retrieveAvailablePriorities,
  retrieveAvailableRequestStates,
  retrieveAvailableReportingPeriods,
} from '@/utils/RequestsOverviewPageUtils';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Column from 'primevue/column';
import DataTable, { type DataTablePageEvent, type DataTableRowClickEvent } from 'primevue/datatable';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import Message from 'primevue/message';
import type { ExtendedStoredRequest, RequestState, RequestPriority } from '@clients/datasourcingservice';
import { type GetDataRequestsDataTypeEnum } from '@clients/communitymanager';

const frameworkFilter = ref();
const datasetsPerPage = 100;
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const waitingForData = ref(true);
const currentChunkIndex = ref(0);
const totalRecords = ref(0);
const rowsPerPage = ref(100);
const firstRowIndex = ref(0);
const currentDataRequests = ref<ExtendedStoredRequest[]>([]);
const searchBarInputEmail = ref('');
const searchBarInputComment = ref('');
const searchBarInputCompanySearchString = ref('');

const availableFrameworks = ref<FrameworkSelectableItem[]>([]);
const selectedFrameworks = ref<FrameworkSelectableItem[]>([]);
const availableRequestStates = ref<SelectableItem[]>([]);
const selectedRequestStates = ref<SelectableItem[]>([]);
const availablePriorities = ref<SelectableItem[]>([]);
const selectedPriorities = ref<SelectableItem[]>([]);
const availableReportingPeriods = ref<SelectableItem[]>([]);
const selectedReportingPeriods = ref<SelectableItem[]>([]);

const numberOfRequestsInformation = computed(() => {
  if (!waitingForData.value) {
    if (totalRecords.value === 0) {
      return 'No results for this search.';
    } else {
      const startIndex = currentChunkIndex.value * rowsPerPage.value + 1;
      const endIndex = Math.min(startIndex + rowsPerPage.value - 1, totalRecords.value);
      return `Showing results ${startIndex}-${endIndex} of ${totalRecords.value}.`;
    }
  }
  return '';
});

/**
 * Sets the current chunk index and first row index to zero.
 */
function setChunkAndFirstRowIndexToZero(): void {
  currentChunkIndex.value = 0;
  firstRowIndex.value = 0;
}

const filterState = computed(() => ({
  frameworks: selectedFrameworks.value,
  requestStates: selectedRequestStates.value,
  priorities: selectedPriorities.value,
  reportingPeriods: selectedReportingPeriods.value,
  email: searchBarInputEmail.value,
  comment: searchBarInputComment.value,
  company: searchBarInputCompanySearchString.value,
}));

watch(filterState, () => {
  setChunkAndFirstRowIndexToZero();
});

onMounted(() => {
  availableFrameworks.value = retrieveAvailableFrameworks();
  availableRequestStates.value = retrieveAvailableRequestStates();
  availablePriorities.value = retrieveAvailablePriorities();
  availableReportingPeriods.value = retrieveAvailableReportingPeriods();
  void getAllRequestsForFilters();
});

/**
 * Fetches all requests from the backend based on the selected filters and search bar inputs.
 */
async function getAllRequestsForFilters(): Promise<void> {
  waitingForData.value = true;
  const selectedFrameworksAsSet = new Set<string>(
    selectedFrameworks.value.map((item) => item.frameworkDataType.toString())
  );
  const selectedRequestStatesAsSet = new Set<RequestState>(
    selectedRequestStates.value.map((item) => item.displayName as RequestState)
  );
  const selectedPriorityAsSet = new Set<RequestPriority>(
    selectedPriorities.value.map((item) => item.displayName as RequestPriority)
  );
  const selectedReportingPeriodAsSet = new Set<string>(selectedReportingPeriods.value.map((item) => item.displayName));

  try {
    if (getKeycloakPromise) {
      const emailFilter = searchBarInputEmail.value || undefined;
      const commentFilter = searchBarInputComment.value || undefined;
      const companySearchStringFilter = searchBarInputCompanySearchString.value || undefined;
      const apiClientProvider = new ApiClientProvider(getKeycloakPromise());

      const frameworksForApi = setToApiArray(selectedFrameworksAsSet);
      const requestStatesForApi = setToApiArray(selectedRequestStatesAsSet);
      const prioritiesForApi = setToApiArray(selectedPriorityAsSet);
      const reportingPeriodsForApi = setToApiArray(selectedReportingPeriodAsSet);

      const filters = {
        dataTypes: frameworksForApi as GetDataRequestsDataTypeEnum[] | undefined,
        requestStates: requestStatesForApi,
        requestPriorities: prioritiesForApi,
        reportingPeriods: reportingPeriodsForApi,
        emailAddress: emailFilter,
        adminComment: commentFilter,
        companySearchString: companySearchStringFilter,
      };

      const [dataResponse, countResponse] = await Promise.all([
        apiClientProvider.apiClients.requestController.postRequestSearch(
          filters,
          datasetsPerPage,
          currentChunkIndex.value
        ),
        apiClientProvider.apiClients.requestController.postRequestCountQuery(filters),
      ]);

      currentDataRequests.value = dataResponse.data;
      totalRecords.value = countResponse.data;
    }
  } catch (error) {
    console.error(error);
  }

  waitingForData.value = false;
}

/**
 * Resets all filters and search bars to their initial state and fetches all requests again.
 */
function resetFilterAndSearchBar(): void {
  currentChunkIndex.value = 0;
  selectedFrameworks.value = [];
  selectedRequestStates.value = [];
  selectedPriorities.value = [];
  selectedReportingPeriods.value = [];
  searchBarInputEmail.value = '';
  searchBarInputComment.value = '';
  searchBarInputCompanySearchString.value = '';
  void getAllRequestsForFilters();
}

/**
 * Converts a set of strings to a comma-separated string for API usage.
 * Returns undefined if the set is empty.
 * @param set The set of strings to convert.
 * @returns A comma-separated string or undefined.
 */
function setToApiArray<T>(set: Set<T>): T[] | undefined {
  return set.size ? Array.from(set) : undefined;
}

/**
 * Handles the pagination event of the data table.
 * @param event
 */
function onPage(event: DataTablePageEvent): void {
  globalThis.scrollTo(0, 0);
  if (event.page != currentChunkIndex.value) {
    currentChunkIndex.value = event.page;
    firstRowIndex.value = currentChunkIndex.value * rowsPerPage.value;
    void getAllRequestsForFilters();
  }
}

/**
 * Handles the row click event of the data table.
 * Navigates to the request detail page of the clicked request.
 * @param event
 */
function onRowClick(event: DataTableRowClickEvent): void {
  const requestIdOfClickedRow = event.data.id;
  router.push(`/requests/${requestIdOfClickedRow}`).catch(console.error);
}
</script>

<style scoped lang="scss">
%search-container-base {
  margin: 0;
  display: flex;
  gap: var(--spacing-lg);
  align-items: start;
}

.search-container-first-line {
  @extend %search-container-base;
  padding: var(--spacing-lg) var(--spacing-lg) var(--spacing-sm) var(--spacing-lg);

  .search-bar {
    width: 20%;
  }
}

.search-container-last-line {
  @extend %search-container-base;
  padding: var(--spacing-sm) var(--spacing-lg) var(--spacing-lg) var(--spacing-lg);

  .search-filter {
    width: 20%;
    text-align: left;
  }
}
</style>
