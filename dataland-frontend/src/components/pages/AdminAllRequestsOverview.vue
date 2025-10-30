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
      <FrameworkDataSearchDropdownFilter
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
      <FrameworkDataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedRequestStates"
        ref="frameworkFilter"
        :available-items="availableRequestStates"
        filter-name="Request State"
        data-test="request-state-picker"
        filter-placeholder="Search by Request State"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} request states"
      />
      <FrameworkDataSearchDropdownFilter
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
      <FrameworkDataSearchDropdownFilter
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
        @click="
          () => {
            setChunkAndFirstRowIndexToZero();
            getAllRequestsForFilters();
          }
        "
        label="FILTER REQUESTS"
      />
    </div>
    <div v-if="waitingForData">
      <p class="font-medium text-xl">Loading...</p>
      <DatalandProgressSpinner />
    </div>

    <div style="padding: var(--spacing-md)">
      <div class="card">
        <DataTable
          v-if="currentDataRequests"
          v-show="!waitingForData"
          ref="dataTable"
          data-test="requests-datatable"
          :value="currentDataRequests"
          :paginator="true"
          :total-records="totalRecords"
          paginator-position="both"
          :rows="rowsPerPage"
          :first="firstRowIndex"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
          :alwaysShowPaginator="true"
          currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
          @row-click="onRowClick($event)"
          @page="onPage($event)"
          class="table-cursor"
          id="admin-request-overview-data"
          :rowHover="true"
          style="cursor: pointer"
        >
          <Column header="REQUESTER" field="userEmailAddress" :sortable="false" />
          <Column header="COMPANY" field="companyName" :sortable="false" />
          <Column header="FRAMEWORK" :sortable="false">
            <template #body="{ data }">
              <div>
                {{ getFrameworkTitle(data.dataType) }}
              </div>
              <div
                data-test="framework-subtitle"
                v-if="frameworkHasSubTitle(data.dataType)"
                style="color: gray; font-size: smaller; line-height: var(--spacing-xs); white-space: nowrap"
              >
                <br />
                {{ getFrameworkSubtitle(data.dataType) }}
              </div>
            </template>
          </Column>
          <Column header="REPORTING PERIOD" field="reportingPeriod" :sortable="false" />
          <Column header="REQUEST ID" field="id" :sortable="false" />
          <Column header="REQUESTED" :sortable="false">
            <template #body="{ data }">
              <div>
                {{ convertUnixTimeInMsToDateString(data.creationTimestamp) }}
              </div>
            </template>
          </Column>
          <Column header="LAST UPDATED" :sortable="false">
            <template #body="{ data }">
              <div>
                {{ convertUnixTimeInMsToDateString(data.lastModifiedDate) }}
              </div>
            </template>
          </Column>
          <Column header="REQUEST STATE" :sortable="false">
            <template #body="{ data }">
              <DatalandTag :severity="data.state" :value="data.state" rounded />
            </template>
          </Column>
          <Column header="REQUEST PRIORITY" :sortable="false">
            <template #body="{ data }">
              <DatalandTag :severity="data.requestPriority" :value="data.requestPriority" />
            </template>
          </Column>
          <Column header="ADMIN COMMENT" :sortable="false" field="adminComment" />
          <template #empty>
            <div style="text-align: center; font-weight: var(--font-weight-bold)">No requests found.</div>
          </template>
        </DataTable>
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatalandTag from '@/components/general/DatalandTag.vue';
import TheContent from '@/components/generics/TheContent.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
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

/**
 * Sets the current chunk index and first row index to zero.
 */
function setChunkAndFirstRowIndexToZero(): void {
  currentChunkIndex.value = 0;
  firstRowIndex.value = 0;
}

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
  const selectedFrameworksForApi = computed<GetDataRequestsDataTypeEnum[] | undefined>(() =>
    selectedFrameworks.value.length
      ? selectedFrameworks.value.map((i) => i.frameworkDataType as GetDataRequestsDataTypeEnum)
      : undefined
  );

  const selectedRequestStatesForApi = computed<RequestState[] | undefined>(() =>
    selectedRequestStates.value.length
      ? selectedRequestStates.value.map((i) => i.displayName as RequestState)
      : undefined
  );

  const selectedPrioritiesForApi = computed<RequestPriority[] | undefined>(() =>
    selectedPriorities.value.length ? selectedPriorities.value.map((i) => i.displayName as RequestPriority) : undefined
  );

  const selectedReportingPeriodsForApi = computed<string[] | undefined>(() =>
    selectedReportingPeriods.value.length ? selectedReportingPeriods.value.map((i) => i.displayName) : undefined
  );

  try {
    if (getKeycloakPromise) {
      const apiClientProvider = new ApiClientProvider(getKeycloakPromise());
      const filters = {
        dataTypes: selectedFrameworksForApi.value,
        requestStates: selectedRequestStatesForApi.value,
        requestPriorities: selectedPrioritiesForApi.value,
        reportingPeriods: selectedReportingPeriodsForApi.value,
        emailAddress: searchBarInputEmail.value || undefined,
        adminComment: searchBarInputComment.value || undefined,
        companySearchString: searchBarInputCompanySearchString.value || undefined,
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
  setChunkAndFirstRowIndexToZero();
  void getAllRequestsForFilters();
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
