<template>
  <TheContent class="min-h-screen relative">
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
        id="framework-filter"
        filter-placeholder="Search by Frameworks"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} frameworks"
      />
      <FrameworkDataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedRequestStatuses"
        ref="frameworkFilter"
        :available-items="availableRequestStatuses"
        filter-name="Request Status"
        data-test="request-status-picker"
        id="framework-filter"
        filter-placeholder="Search by Request Status"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} request status"
      />
      <FrameworkDataSearchDropdownFilter
        :disabled="waitingForData"
        v-model="selectedPriorities"
        ref="frameworkFilter"
        :available-items="availablePriorities"
        filter-name="Priority"
        data-test="request-priority-picker"
        id="framework-filter"
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
        id="framework-filter"
        filter-placeholder="Search by Reporting Period"
        class="search-filter"
        :max-selected-labels="1"
        selected-items-label="{0} reporting periods"
      />
      <PrimeButton variant="link" @click="resetFilterAndSearchBar" label="RESET" data-test="reset-filter" />
      <PrimeButton
        :disabled="waitingForData"
        data-test="trigger-filtering-requests"
        @click="getAllRequestsForFilters"
        label="FILTER REQUESTS "
      />
    </div>
    <div class="message-container">
      <Message class="info-message" variant="simple" severity="secondary">{{ numberOfRequestsInformation }}</Message>
    </div>

    <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
      <p class="font-medium text-xl">Loading...</p>
      <DatalandProgressSpinner />
    </div>

    <div class="col-12 text-left p-3">
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
                style="color: gray; font-size: smaller; line-height: 0.5; white-space: nowrap"
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
              {{ slotProps.data.dataRequestId }}
            </template>
          </Column>
          <Column header="REQUESTED" field="creationTimestamp" :sortable="false">
            <template #body="slotProps">
              <div>
                {{ convertUnixTimeInMsToDateString(slotProps.data.creationTimestamp) }}
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
          <Column header="REQUEST STATUS" :sortable="false" field="requestStatus">
            <template #body="slotProps">
              <DatalandTag :severity="slotProps.data.requestStatus" :value="slotProps.data.requestStatus" rounded />
            </template>
          </Column>
          <Column header="ACCESS STATUS" :sortable="false" field="accessStatus">
            <template #body="slotProps">
              <DatalandTag :severity="slotProps.data.accessStatus" :value="slotProps.data.accessStatus" />
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
          <div class="d-center-div text-center px-7 py-4">
            <p class="font-medium text-xl">There are no data requests on Dataland matching your filters.</p>
          </div>
        </div>
      </div>
    </div>
  </TheContent>
</template>

<script lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatalandTag from '@/components/general/DatalandTagLegacy.vue';
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
import type { DataTypeEnum } from '@clients/backend';
import {
  type ExtendedStoredDataRequest,
  type GetDataRequestsDataTypeEnum,
  type RequestPriority,
  type RequestStatus,
} from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Column from 'primevue/column';
import DataTable, { type DataTablePageEvent, type DataTableRowClickEvent } from 'primevue/datatable';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import Message from 'primevue/message';
import { defineComponent, inject, ref } from 'vue';

export default defineComponent({
  name: 'AdminDataRequestsOverview',
  components: {
    DatalandProgressSpinner,
    DatalandTag,
    PrimeButton,
    FrameworkDataSearchDropdownFilter,
    TheContent,
    DataTable,
    Column,
    IconField,
    InputText,
    InputIcon,
    Message,
  },

  setup() {
    return {
      frameworkFilter: ref(),
      datasetsPerPage: 100,
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },

  data() {
    return {
      waitingForData: true,
      currentChunkIndex: 0,
      totalRecords: 0,
      rowsPerPage: 100,
      firstRowIndex: 0,
      currentDataRequests: [] as ExtendedStoredDataRequest[],
      searchBarInputEmail: '',
      searchBarInputComment: '',
      searchBarInputCompanySearchString: '',
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      availableRequestStatuses: [] as Array<SelectableItem>,
      selectedRequestStatuses: [] as Array<SelectableItem>,
      availablePriorities: [] as Array<SelectableItem>,
      selectedPriorities: [] as Array<SelectableItem>,
      availableReportingPeriods: [] as Array<SelectableItem>,
      selectedReportingPeriods: [] as Array<SelectableItem>,
    };
  },
  mounted() {
    this.availableFrameworks = retrieveAvailableFrameworks();
    this.availableRequestStatuses = retrieveAvailableRequestStates();
    this.availablePriorities = retrieveAvailablePriorities();
    this.availableReportingPeriods = retrieveAvailableReportingPeriods();
    this.getAllRequestsForFilters().catch((error) => console.error(error));
  },
  computed: {
    numberOfRequestsInformation(): string {
      if (!this.waitingForData) {
        if (this.totalRecords === 0) {
          return 'No results for this search.';
        } else {
          const startIndex = this.currentChunkIndex * this.rowsPerPage + 1;
          const endIndex = Math.min(startIndex + this.rowsPerPage - 1, this.totalRecords);
          return `Showing results ${startIndex}-${endIndex} of ${this.totalRecords}.`;
        }
      }
      return '';
    },
  },

  watch: {
    selectedFrameworks(newSelected) {
      this.selectedFrameworks = newSelected;
      this.setChunkAndFirstRowIndexToZero();
    },
    selectedRequestStatuses(newSelected) {
      this.selectedRequestStatuses = newSelected;
      this.setChunkAndFirstRowIndexToZero();
    },
    selectedPriorities(newSelected) {
      this.selectedPriorities = newSelected;
      this.setChunkAndFirstRowIndexToZero();
    },
    selectedReportingPeriods(newSelected) {
      this.selectedReportingPeriods = newSelected;
      this.setChunkAndFirstRowIndexToZero();
    },
    searchBarInputEmail(newSearch: string) {
      this.searchBarInputEmail = newSearch;
      this.setChunkAndFirstRowIndexToZero();
    },
    searchBarInputComment(newSearch: string) {
      this.searchBarInputComment = newSearch;
      this.setChunkAndFirstRowIndexToZero();
    },
    searchBarInputCompanySearchString(newSearch: string) {
      this.searchBarInputCompanySearchString = newSearch;
      this.setChunkAndFirstRowIndexToZero();
    },
  },
  methods: {
    frameworkHasSubTitle,
    getFrameworkTitle,
    getFrameworkSubtitle,
    convertUnixTimeInMsToDateString,

    /**
     * Gets list of storedDataRequests
     */
    async getAllRequestsForFilters() {
      this.waitingForData = true;
      const selectedFrameworksAsSet = new Set<DataTypeEnum>(
        this.selectedFrameworks.map((selectableItem) => selectableItem.frameworkDataType)
      );
      const selectedRequestStatusesAsSet = new Set<RequestStatus>(
        this.selectedRequestStatuses.map((selectableItem) => selectableItem.displayName as RequestStatus)
      );
      const selectedPriorityAsSet = new Set<RequestPriority>(
        this.selectedPriorities.map((selectableItem) => selectableItem.displayName as RequestPriority)
      );
      const selectedReportingPeriodAsSet = new Set<string>(
        this.selectedReportingPeriods.map((selectableItem) => selectableItem.displayName)
      );
      try {
        if (this.getKeycloakPromise) {
          const emailFilter = this.searchBarInputEmail === '' ? undefined : this.searchBarInputEmail;
          const commentFilter = this.searchBarInputComment === '' ? undefined : this.searchBarInputComment;
          const companySearchStringFilter =
            this.searchBarInputCompanySearchString === '' ? undefined : this.searchBarInputCompanySearchString;
          const apiClientProvider = new ApiClientProvider(this.getKeycloakPromise());
          this.currentDataRequests = (
            await apiClientProvider.apiClients.communityManagerRequestController.getDataRequests(
              selectedFrameworksAsSet as Set<GetDataRequestsDataTypeEnum>,
              undefined,
              emailFilter,
              commentFilter,
              selectedRequestStatusesAsSet,
              undefined,
              selectedPriorityAsSet,
              selectedReportingPeriodAsSet,
              undefined,
              companySearchStringFilter,
              this.datasetsPerPage,
              this.currentChunkIndex
            )
          ).data;
          this.totalRecords = (
            await apiClientProvider.apiClients.communityManagerRequestController.getNumberOfRequests(
              selectedFrameworksAsSet as Set<GetDataRequestsDataTypeEnum>,
              undefined,
              emailFilter,
              commentFilter,
              selectedRequestStatusesAsSet,
              undefined,
              selectedPriorityAsSet,
              selectedReportingPeriodAsSet,
              undefined,
              companySearchStringFilter
            )
          ).data;
        }
      } catch (error) {
        console.error(error);
      }
      this.waitingForData = false;
    },

    /**
     * Resets selected frameworks and searchBarInput
     */
    resetFilterAndSearchBar() {
      this.currentChunkIndex = 0;
      this.selectedFrameworks = [];
      this.selectedRequestStatuses = [];
      this.selectedPriorities = [];
      this.selectedReportingPeriods = [];
      this.searchBarInputEmail = '';
      this.searchBarInputComment = '';
      this.searchBarInputCompanySearchString = '';
      this.getAllRequestsForFilters().catch((error) => console.error(error));
    },

    /**
     * Updates the current Page
     * @param event DataTablePageEvent
     */
    onPage(event: DataTablePageEvent) {
      globalThis.scrollTo(0, 0);
      if (event.page != this.currentChunkIndex) {
        this.currentChunkIndex = event.page;
        this.firstRowIndex = this.currentChunkIndex * this.rowsPerPage;
        this.getAllRequestsForFilters().catch((error) => console.error(error));
      }
    },

    /**
     * Navigates to the view dataRequest page
     * @param event contains column that was clicked
     * @returns the promise of the router push action
     */
    onRowClick(event: DataTableRowClickEvent) {
      const requestIdOfClickedRow = event.data.dataRequestId;
      return router.push(`/requests/${requestIdOfClickedRow}`);
    },

    /**
     * Sets the currentChunkIndex and firstRowIndex to Zero
     */
    setChunkAndFirstRowIndexToZero() {
      this.currentChunkIndex = 0;
      this.firstRowIndex = 0;
    },
  },
});
</script>

<style scoped lang="scss">
%search-container-base {
  margin: 0;
  width: 100%;
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

  > :last-child {
    margin-left: auto;
  }
}

.message-container {
  width: 100%;
  display: flex;
  justify-content: end;
  margin-bottom: var(--spacing-lg);

  .info-message {
    margin: 0 var(--spacing-lg);
  }
}

.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}
</style>
