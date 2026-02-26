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
          @keyup.enter="resetChunkAndFirstRowIndexAndGetAllRequests"
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
          @keyup.enter="resetChunkAndFirstRowIndexAndGetAllRequests"
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
          @keyup.enter="resetChunkAndFirstRowIndexAndGetAllRequests"
        />
      </IconField>
    </div>
    <div class="search-container-last-line">
      <FrameworkDataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedFrameworks"
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
        v-model="selectedMixedStates"
        :available-items="availableMixedStates"
        filter-name="State"
        data-test="state-picker"
        filter-placeholder="Search by State"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} states"
      />
      <FrameworkDataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedPriorities"
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
        @click="resetChunkAndFirstRowIndexAndGetAllRequests"
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
          :lazy="true"
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
          <Column v-if="isColumnVisible('requester')" header="REQUESTER" field="userEmailAddress" :sortable="false" />
          <Column v-if="isColumnVisible('company')" header="COMPANY" field="companyName" :sortable="false" />
          <Column v-if="isColumnVisible('framework')" header="FRAMEWORK" :sortable="false">
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
          <Column
            v-if="isColumnVisible('reportingPeriod')"
            header="REPORTING PERIOD"
            field="reportingPeriod"
            :sortable="false"
          />
          <Column v-if="isColumnVisible('id')" header="REQUEST ID" field="id" :sortable="false" />
          <Column v-if="isColumnVisible('creationTimestamp')" header="REQUESTED" :sortable="false" class="date-column">
            <template #body="{ data }">
              <div>
                <span>{{ convertUnixTimeInMsToDateString(data.creationTimestamp) }}</span>
              </div>
            </template>
          </Column>
          <Column
            v-if="isColumnVisible('lastModifiedDate')"
            header="LAST UPDATED"
            :sortable="false"
            class="date-column"
          >
            <template #body="{ data }">
              <div>
                <span>{{ convertUnixTimeInMsToDateString(data.lastModifiedDate) }}</span>
              </div>
            </template>
          </Column>
          <Column v-if="isColumnVisible('state')" header="STATE" :sortable="false">
            <template #body="{ data }">
              <DatalandTag
                :severity="getDisplayedState(data)"
                :value="getDisplayedStateLabel(getDisplayedState(data))"
              />
            </template>
          </Column>
          <Column v-if="isColumnVisible('nextSourcingAttempt')" header="NEXT SOURCING ATTEMPT" :sortable="false">
            <template #body="{ data }">
              <div v-if="data.dataSourcingDetails?.dateOfNextDocumentSourcingAttempt">
                <span
                  >{{
                    dateStringFormatter(data.dataSourcingDetails.dateOfNextDocumentSourcingAttempt).split(', ')[0]
                  }},</span
                >
                <span>{{
                  dateStringFormatter(data.dataSourcingDetails.dateOfNextDocumentSourcingAttempt)
                    .split(', ')
                    .slice(1)
                    .join(', ')
                }}</span>
              </div>
              <template v-else>-</template>
            </template>
          </Column>
          <Column v-if="isColumnVisible('priority')" header="REQUEST PRIORITY" :sortable="false">
            <template #body="{ data }">
              <DatalandTag :severity="data.requestPriority" :value="data.requestPriority" />
            </template>
          </Column>
          <Column v-if="isColumnVisible('documentCollector')" header="DOCUMENT COLLECTOR" :sortable="false">
            <template #body="{ data }">
              {{ data.dataSourcingDetails?.documentCollectorName ?? '-' }}
            </template>
          </Column>
          <Column v-if="isColumnVisible('dataExtractor')" header="DATA EXTRACTOR" :sortable="false">
            <template #body="{ data }">
              {{ data.dataSourcingDetails?.dataExtractorName ?? '-' }}
            </template>
          </Column>
          <Column
            v-if="isColumnVisible('adminComment')"
            header="ADMIN COMMENT"
            :sortable="false"
            field="adminComment"
          />
          <template #paginatorstart>
            <span class="paginator-spacer"></span>
          </template>
          <template #paginatorend>
            <div class="column-selector-container" @click="toggleColumnPopover">
              <span class="column-selector-label">Select Columns</span>
              <PrimeButton
                type="button"
                icon="pi pi-cog"
                variant="text"
                class="column-selector-button"
                data-test="column-selector-button"
                aria-label="Configure columns"
              />
            </div>
            <Popover ref="columnPopover" data-test="column-selector-popover">
              <div class="column-popover-content">
                <div v-for="col in allColumns" :key="col.field" class="column-checkbox-row">
                  <Checkbox
                    v-model="selectedColumns"
                    :inputId="col.field"
                    :value="col"
                    :data-test="`column-checkbox-${col.field}`"
                  />
                  <label :for="col.field">{{ col.header }}</label>
                </div>
              </div>
            </Popover>
          </template>
          <template #empty>
            <div style="text-align: center; font-weight: var(--font-weight-bold)">No requests found.</div>
          </template>
        </DataTable>
      </div>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject, watch } from 'vue';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatalandTag from '@/components/general/DatalandTag.vue';
import TheContent from '@/components/generics/TheContent.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import { useRouter } from 'vue-router';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString, dateStringFormatter } from '@/utils/DataFormatUtils';
import type {
  FrameworkSelectableItem,
  DisplayedStateSelectableItem,
  SelectableItem,
} from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import {
  retrieveAvailableFrameworks,
  retrieveAvailablePriorities,
  retrieveAvailableReportingPeriods,
  retrieveAvailableDisplayedStates,
  convertDisplayedStatesToApiFilters,
  getDisplayedState,
  getDisplayedStateLabel,
} from '@/utils/RequestsOverviewPageUtils';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Column from 'primevue/column';
import DataTable, { type DataTablePageEvent, type DataTableRowClickEvent } from 'primevue/datatable';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import Checkbox from 'primevue/checkbox';
import Popover from 'primevue/popover';
import { type RequestPriority, type DataSourcingEnhancedRequest } from '@clients/datasourcingservice';
import { type GetDataRequestsDataTypeEnum } from '@clients/communitymanager';

const datasetsPerPage = 100;
const COLUMN_SELECTION_STORAGE_KEY = 'adminAllRequestsOverview.selectedColumns';
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const router = useRouter();

const waitingForData = ref(true);
const currentChunkIndex = ref(0);
const totalRecords = ref(0);
const rowsPerPage = ref(datasetsPerPage);
const firstRowIndex = ref(0);
const currentDataRequests = ref<DataSourcingEnhancedRequest[]>([]);
const searchBarInputEmail = ref('');
const searchBarInputComment = ref('');
const searchBarInputCompanySearchString = ref('');

const availableFrameworks = ref<FrameworkSelectableItem[]>([]);
const selectedFrameworks = ref<FrameworkSelectableItem[]>([]);
const availableMixedStates = ref<DisplayedStateSelectableItem[]>([]);
const selectedMixedStates = ref<DisplayedStateSelectableItem[]>([]);
const availablePriorities = ref<SelectableItem[]>([]);
const selectedPriorities = ref<SelectableItem[]>([]);
const availableReportingPeriods = ref<SelectableItem[]>([]);
const selectedReportingPeriods = ref<SelectableItem[]>([]);

interface ColumnDefinition {
  field: string;
  header: string;
}

const allColumns: ColumnDefinition[] = [
  { field: 'requester', header: 'Requester' },
  { field: 'company', header: 'Company' },
  { field: 'framework', header: 'Framework' },
  { field: 'reportingPeriod', header: 'Reporting Period' },
  { field: 'id', header: 'Request ID' },
  { field: 'creationTimestamp', header: 'Requested' },
  { field: 'lastModifiedDate', header: 'Last Updated' },
  { field: 'state', header: 'State' },
  { field: 'nextSourcingAttempt', header: 'Next Sourcing Attempt' },
  { field: 'priority', header: 'Request Priority' },
  { field: 'documentCollector', header: 'Document Collector' },
  { field: 'dataExtractor', header: 'Data Extractor' },
  { field: 'adminComment', header: 'Admin Comment' },
];

const selectedColumns = ref<ColumnDefinition[]>(loadColumnSelection());
const columnPopover = ref();

/**
 * Toggles the column selection popover visibility.
 * @param event - The click event from the button
 */
function toggleColumnPopover(event: Event): void {
  columnPopover.value.toggle(event);
}

/**
 * Loads the column selection from localStorage, falling back to all columns if not found.
 * @returns The saved column selection or all columns as default
 */
function loadColumnSelection(): ColumnDefinition[] {
  const saved = localStorage.getItem(COLUMN_SELECTION_STORAGE_KEY);
  if (saved) {
    const savedFields: string[] = JSON.parse(saved);
    return allColumns.filter((col) => savedFields.includes(col.field));
  }
  return [...allColumns];
}

watch(
  selectedColumns,
  (newSelection) => {
    const fields = newSelection.map((col) => col.field);
    localStorage.setItem(COLUMN_SELECTION_STORAGE_KEY, JSON.stringify(fields));
  },
  { deep: true }
);

/**
 * Checks if a column is currently visible based on user selection.
 * @param field - The field identifier of the column
 * @returns true if the column is selected for display
 */
function isColumnVisible(field: string): boolean {
  return selectedColumns.value.some((col) => col.field === field);
}

/**
 * Sets the current chunk index and first row index to zero.
 */
function setChunkAndFirstRowIndexToZero(): void {
  currentChunkIndex.value = 0;
  firstRowIndex.value = 0;
}

onMounted(() => {
  availableFrameworks.value = retrieveAvailableFrameworks();
  availableMixedStates.value = retrieveAvailableDisplayedStates();
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

  const mixedStateFilters = computed(() => convertDisplayedStatesToApiFilters(selectedMixedStates.value));

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
        requestStates: mixedStateFilters.value.requestStates,
        dataSourcingStates: mixedStateFilters.value.dataSourcingStates,
        requestPriorities: selectedPrioritiesForApi.value,
        reportingPeriods: selectedReportingPeriodsForApi.value,
        emailAddress: searchBarInputEmail.value || undefined,
        adminComment: searchBarInputComment.value || undefined,
        companySearchString: searchBarInputCompanySearchString.value || undefined,
      };

      const [dataResponse, countResponse] = await Promise.all([
        apiClientProvider.apiClients.enhancedRequestController.postRequestSearch(
          filters,
          datasetsPerPage,
          currentChunkIndex.value
        ),
        apiClientProvider.apiClients.enhancedRequestController.postRequestCountQuery(filters),
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
  selectedMixedStates.value = [];
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
 * Resets the chunk index and first row index to zero and fetches all requests again.
 */
function resetChunkAndFirstRowIndexAndGetAllRequests(): void {
  setChunkAndFirstRowIndexToZero();
  void getAllRequestsForFilters();
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

  .search-filter {
    width: 20%;
    text-align: left;
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

.paginator-spacer {
  width: 3rem;
}

:deep(.p-datatable-paginator-bottom .column-selector-container) {
  visibility: hidden;
}

:deep(.p-paginator-content-end) {
  flex: none;
}

.column-selector-container {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  cursor: pointer;
}

.column-selector-label {
  font-size: 0.875rem;
  color: var(--p-primary-color);
}

:deep(.column-selector-button) {
  pointer-events: none;

  &:hover {
    background: transparent;
  }

  .pi {
    font-size: 1.25rem;
  }
}

.column-popover-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);

  .column-checkbox-row {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);

    label {
      cursor: pointer;
    }
  }
}
</style>
