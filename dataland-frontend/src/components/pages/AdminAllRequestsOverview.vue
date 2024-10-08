<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="5">
      <TheContent class="min-h-screen paper-section relative">
        <div>
          <div
            id="searchBarAndFiltersContainer"
            class="w-full bg-white pt-4 justify-between"
            ref="searchBarAndFiltersContainer"
          >
            <span class="align-content-start flex items-center justify-start">
              <span class="w-3 p-input-icon-left" style="margin: 15px">
                <i class="pi pi-search pl-3 pr-3" aria-hidden="true" style="color: #958d7c" />
                <InputText
                  data-test="email-searchbar"
                  v-model="searchBarInput"
                  placeholder="Search by Requester"
                  class="w-12 pl-6 pr-6"
                />
              </span>
              <FrameworkDataSearchDropdownFilter
                v-model="selectedFrameworks"
                ref="frameworkFilter"
                :available-items="availableFrameworks"
                filter-name="Framework"
                data-test="framework-picker"
                filter-id="framework-filter"
                filter-placeholder="Search by Frameworks"
                class="ml-3"
                style="margin: 15px"
              />
              <FrameworkDataSearchDropdownFilter
                v-model="selectedRequestStatus"
                ref="frameworkFilter"
                :available-items="availableRequestStatus"
                filter-name="Request Status"
                data-test="request-status-picker"
                filter-id="framework-filter"
                filter-placeholder="Search by Request Status"
                class="ml-3"
                style="margin: 15px"
              />
              <div class="flex align-items-center">
                <span
                  data-test="reset-filter"
                  style="margin: 15px"
                  class="ml-3 cursor-pointer text-primary font-semibold d-letters"
                  @click="resetFilterAndSearchBar"
                  >RESET</span
                >
              </div>
              <div class="flex align-items-center ml-auto" style="margin: 15px">
                <span>{{ numberOfRequestsInformation }}</span>
              </div>
            </span>
          </div>

          <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
            <p class="font-medium text-xl">Loading...</p>
            <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
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
                    </div></template
                  >
                </Column>
                <Column header="LAST UPDATED" :sortable="false" field="lastModifiedDate">
                  <template #body="slotProps"
                    ><div>
                      {{ convertUnixTimeInMsToDateString(slotProps.data.lastModifiedDate) }}
                    </div>
                  </template>
                </Column>
                <Column header="REQUEST STATUS" :sortable="false" field="requestStatus">
                  <template #body="slotProps">
                    <div :class="badgeClass(slotProps.data.requestStatus)" style="display: inline-flex">
                      {{ slotProps.data.requestStatus }}
                    </div>
                  </template>
                </Column>
                <Column header="ACCESS STATUS" :sortable="false" field="accessStatus">
                  <template #body="slotProps">
                    <div :class="accessStatusBadgeClass(slotProps.data.accessStatus)" style="display: inline-flex">
                      {{ slotProps.data.accessStatus }}
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
        </div>
      </TheContent>
    </DatasetsTabMenu>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import TheContent from '@/components/generics/TheContent.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import { defineComponent, inject, ref } from 'vue';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import DataTable, { type DataTablePageEvent, type DataTableRowClickEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import { frameworkHasSubTitle, getFrameworkSubtitle, getFrameworkTitle } from '@/utils/StringFormatter';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import {
  type ExtendedStoredDataRequest,
  type GetDataRequestsDataTypeEnum,
  type RequestStatus,
} from '@clients/communitymanager';
import InputText from 'primevue/inputtext';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import type { FrameworkSelectableItem, SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { accessStatusBadgeClass, badgeClass } from '@/utils/RequestUtils';
import { retrieveAvailableFrameworks, retrieveAvailableRequestStatus } from '@/utils/RequestsOverviewPageUtils';
import type { DataTypeEnum } from '@clients/backend';
import router from '@/router';

export default defineComponent({
  name: 'AdminDataRequestsOverview',
  components: {
    AuthenticationWrapper,
    FrameworkDataSearchDropdownFilter,
    DatasetsTabMenu,
    TheFooter,
    TheContent,
    TheHeader,
    DataTable,
    Column,
    InputText,
  },

  setup() {
    return {
      frameworkFilter: ref(),
      datasetsPerPage: 100,
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },

  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      waitingForData: true,
      currentChunkIndex: 0,
      totalRecords: 0,
      rowsPerPage: 100,
      firstRowIndex: 0,
      currentDataRequests: [] as ExtendedStoredDataRequest[],
      footerContent,
      searchBarInput: '',
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      availableRequestStatus: [] as Array<SelectableItem>,
      selectedRequestStatus: [] as Array<SelectableItem>,
      debounceInMs: 300,
      timerId: 0,
    };
  },
  mounted() {
    this.availableFrameworks = retrieveAvailableFrameworks();
    this.availableRequestStatus = retrieveAvailableRequestStatus();
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
    selectedFrameworks() {
      this.currentChunkIndex = 0;
      this.firstRowIndex = 0;
      if (!this.waitingForData) {
        this.getAllRequestsForFilters();
      }
    },
    selectedRequestStatus() {
      this.currentChunkIndex = 0;
      this.firstRowIndex = 0;
      if (!this.waitingForData) {
        this.getAllRequestsForFilters();
      }
    },
    searchBarInput(newSearch: string) {
      this.searchBarInput = newSearch;
      this.currentChunkIndex = 0;
      this.firstRowIndex = 0;
      if (this.timerId) {
        clearTimeout(this.timerId);
      }
      this.timerId = setTimeout(() => this.getAllRequestsForFilters(), this.debounceInMs);
    },
  },
  methods: {
    badgeClass,
    accessStatusBadgeClass,
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
      try {
        if (this.getKeycloakPromise) {
          const emailFilter = this.searchBarInput === '' ? undefined : this.searchBarInput;
          const apiClientProvider = new ApiClientProvider(this.getKeycloakPromise());
          this.currentDataRequests = (
            await apiClientProvider.apiClients.requestController.getDataRequests(
              selectedFrameworksAsSet as Set<GetDataRequestsDataTypeEnum>,
              undefined,
              emailFilter,
              selectedRequestStatusesAsSet,
              undefined,
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
              selectedRequestStatusesAsSet,
              undefined,
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
      this.searchBarInput = '';
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
        this.getAllRequestsForFilters();
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
  },
});
</script>
