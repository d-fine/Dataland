<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="6">
      <TheContent class="min-h-screen relative">
        <div class="search-container">
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
            v-model="selectedRequestStatus"
            ref="frameworkFilter"
            :available-items="availableRequestStatus"
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
            v-model="selectedPriority"
            ref="frameworkFilter"
            :available-items="availablePriority"
            filter-name="Priority"
            data-test="request-priority-picker"
            id="framework-filter"
            filter-placeholder="Search by Priority"
            class="search-filter"
            :max-selected-labels="1"
            selected-items-label="{0} request priorities"
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
          <Message class="info-message" variant="simple" severity="secondary">{{
            numberOfRequestsInformation
          }}</Message>
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
                  <DatalandTag :severity="slotProps.data.requestStatus" :value="slotProps.data.requestStatus" />
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
    </DatasetsTabMenu>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatalandTag from '@/components/general/DatalandTag.vue';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import type { FrameworkSelectableItem, SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import {
  retrieveAvailableFrameworks,
  retrieveAvailablePriority,
  retrieveAvailableRequestStatus,
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
import { defineComponent, inject, ref } from 'vue';

export default defineComponent({
  name: 'AdminDataRequestsOverview',
  components: {
    DatalandProgressSpinner,
    DatalandTag,
    PrimeButton,
    AuthenticationWrapper,
    FrameworkDataSearchDropdownFilter,
    DatasetsTabMenu,
    TheFooter,
    TheContent,
    TheHeader,
    DataTable,
    Column,
    IconField,
    InputText,
    InputIcon,
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
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      availableRequestStatus: [] as Array<SelectableItem>,
      selectedRequestStatus: [] as Array<SelectableItem>,
      availablePriority: [] as Array<SelectableItem>,
      selectedPriority: [] as Array<SelectableItem>,
    };
  },
  mounted() {
    this.availableFrameworks = retrieveAvailableFrameworks();
    this.availableRequestStatus = retrieveAvailableRequestStatus();
    this.availablePriority = retrieveAvailablePriority();
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
    selectedRequestStatus(newSelected) {
      this.selectedRequestStatus = newSelected;
      this.setChunkAndFirstRowIndexToZero();
    },
    selectedPriority(newSelected) {
      this.selectedPriority = newSelected;
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
        this.selectedRequestStatus.map((selectableItem) => selectableItem.displayName as RequestStatus)
      );
      const selectedPriorityAsSet = new Set<RequestPriority>(
        this.selectedPriority.map((selectableItem) => selectableItem.displayName as RequestPriority)
      );
      try {
        if (this.getKeycloakPromise) {
          const emailFilter = this.searchBarInputEmail === '' ? undefined : this.searchBarInputEmail;
          const commentFilter = this.searchBarInputComment === '' ? undefined : this.searchBarInputComment;
          const apiClientProvider = new ApiClientProvider(this.getKeycloakPromise());
          this.currentDataRequests = (
            await apiClientProvider.apiClients.requestController.getDataRequests(
              selectedFrameworksAsSet as Set<GetDataRequestsDataTypeEnum>,
              undefined,
              emailFilter,
              commentFilter,
              selectedRequestStatusesAsSet,
              undefined,
              selectedPriorityAsSet,
              undefined,
              undefined,
              this.datasetsPerPage,
              this.currentChunkIndex
            )
          ).data;
          this.totalRecords = (
            await apiClientProvider.apiClients.requestController.getNumberOfRequests(
              selectedFrameworksAsSet as Set<GetDataRequestsDataTypeEnum>,
              undefined,
              emailFilter,
              commentFilter,
              selectedRequestStatusesAsSet,
              undefined,
              selectedPriorityAsSet,
              undefined,
              undefined
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
      this.selectedRequestStatus = [];
      this.selectedPriority = [];
      this.searchBarInputEmail = '';
      this.searchBarInputComment = '';
      void this.getAllRequestsForFilters();
    },

    /**
     * Updates the current Page
     * @param event DataTablePageEvent
     */
    onPage(event: DataTablePageEvent) {
      window.scrollTo(0, 0);
      if (event.page != this.currentChunkIndex) {
        this.currentChunkIndex = event.page;
        this.firstRowIndex = this.currentChunkIndex * this.rowsPerPage;
        void this.getAllRequestsForFilters();
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
<style scoped>
.search-container {
  margin: 0;
  width: 100%;
  padding: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-lg);
  align-items: start;

  .search-bar {
    width: 20%;
  }

  .search-filter {
    width: 10%;
    text-align: left;
  }

  :last-child {
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
