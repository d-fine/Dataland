<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="3">
      <TheContent class="min-h-screen paper-section relative">
        <div v-if="waitingForData || storedDataRequests.length > 0">
          <div
            id="searchBarAndFiltersContainer"
            class="w-full bg-white pt-4 justify-between"
            ref="searchBarAndFiltersContainer"
          >
            <span class="align-content-start flex items-center justify-start">
              <span class="w-3 p-input-icon-left" style="margin: 15px">
                <i class="pi pi-search pl-3 pr-3" aria-hidden="true" style="color: #958d7c" />
                <InputText
                  data-test="requested-Datasets-searchbar"
                  v-model="searchBarInput"
                  placeholder="Search by company name"
                  class="w-12 pl-6 pr-6"
                />
              </span>
              <FrameworkDataSearchDropdownFilter
                v-model="selectedFrameworks"
                ref="frameworkFilter"
                :available-items="availableFrameworks"
                filter-name="Framework"
                data-test="requested-Datasets-frameworks"
                filter-id="framework-filter"
                filter-placeholder="Search frameworks"
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
            </span>
          </div>
          <div class="col-12 text-left p-3">
            <div class="card">
              <DataTable
                :value="displayedData"
                style="cursor: pointer"
                :rowHover="true"
                :loading="waitingForData"
                data-test="requested-Datasets-table"
                paginator
                paginator-position="bottom"
                :rows="datasetsPerPage"
                lazy
                :total-records="numberOfFilteredRequests"
                @page="onPage($event)"
                @sort="onSort($event)"
                @row-click="onRowClick($event)"
                id="my-data-requests-overview-table"
              >
                <Column header="COMPANY" field="companyName" :sortable="true">
                  <template #body="slotProps">
                    {{ slotProps.data.companyName }}
                  </template>
                </Column>
                <Column header="FRAMEWORK" :sortable="true" field="dataType">
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
                <Column header="REPORTING PERIOD" field="reportingPeriod" :sortable="true">
                  <template #body="slotProps">
                    {{ slotProps.data.reportingPeriod }}
                  </template>
                </Column>
                <Column header="REQUESTED" field="creationTimestamp" :sortable="true">
                  <template #body="slotProps">
                    <div>
                      {{ convertUnixTimeInMsToDateString(slotProps.data.creationTimestamp) }}
                    </div></template
                  >
                </Column>
                <Column header="LAST UPDATED" :sortable="true" field="lastModifiedDate">
                  <template #body="slotProps"
                    ><div>
                      {{ convertUnixTimeInMsToDateString(slotProps.data.lastModifiedDate) }}
                    </div>
                  </template>
                </Column>
                <Column header="STATUS" :sortable="true" field="requestStatus">
                  <template #body="slotProps">
                    <div :class="badgeClass(slotProps.data.requestStatus)" style="display: inline-flex">
                      {{ slotProps.data.requestStatus }}
                    </div>
                  </template>
                </Column>
                <Column field="resolve" header="">
                  <template #body="slotProps">
                    <div
                      v-if="slotProps.data.requestStatus == RequestStatus.Answered"
                      class="text-right text-primary no-underline font-bold"
                    >
                      <span
                        id="resolveButton"
                        style="cursor: pointer"
                        data-test="requested-Datasets-Resolve"
                        @click="goToResolveDataRequestPage(slotProps.data.datalandCompanyId, slotProps.data.dataType)"
                        >RESOLVE</span
                      >
                      <span class="ml-3">></span>
                    </div>
                  </template>
                </Column>
              </DataTable>
            </div>
          </div>
        </div>
        <div v-if="!waitingForData && storedDataRequests.length == 0">
          <div class="d-center-div text-center px-7 py-4">
            <p class="font-medium text-xl">You have not requested data yet.</p>
            <p class="font-medium text-xl">Request data to see your requests here.</p>
            <a @click="goToBulkDataRequestPage()" class="no-underline" data-test="bulkDataRequestButton">
              <button
                class="p-button p-component uppercase p-button p-button-sm mr-3"
                type="button"
                data-pc-name="button"
                data-pc-section="root"
              >
                <i class="material-icons"> add_box </i><span class="d-letters pl-2"> BULK DATA REQUEST </span>
              </button></a
            >
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
import DataTable, {
  type DataTablePageEvent,
  type DataTableRowClickEvent,
  type DataTableSortEvent,
} from 'primevue/datatable';
import Column from 'primevue/column';
import {
  frameworkHasSubTitle,
  getFrameworkSubtitle,
  getFrameworkTitle,
  humanizeStringOrNumber,
} from '@/utils/StringFormatter';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { type ExtendedStoredDataRequest, RequestStatus } from '@clients/communitymanager';
import { type DataTypeEnum } from '@clients/backend';
import InputText from 'primevue/inputtext';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import type { FrameworkSelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { badgeClass } from '@/utils/RequestUtils';

export default defineComponent({
  name: 'MyDataRequestsOverview',
  computed: {
    RequestStatus() {
      return RequestStatus;
    },
  },
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
      currentPage: 0,
      storedDataRequests: [] as ExtendedStoredDataRequest[],
      displayedData: [] as ExtendedStoredDataRequest[],
      footerContent,
      searchBarInput: '',
      searchBarInputFilter: '',
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      numberOfFilteredRequests: 0,
      sortField: 'requestStatus' as keyof ExtendedStoredDataRequest,
      sortOrder: 1,
    };
  },
  mounted() {
    this.availableFrameworks = this.retrieveAvailableFrameworks();
    this.getStoredRequestDataList().catch((error) => console.error(error));
    this.resetFilterAndSearchBar();
  },
  watch: {
    selectedFrameworks() {
      this.updateCurrentDisplayedData();
    },
    waitingForData() {
      this.updateCurrentDisplayedData();
    },
    searchBarInput(newSearch: string) {
      this.searchBarInputFilter = newSearch;
      this.updateCurrentDisplayedData();
    },
  },
  methods: {
    badgeClass,
    frameworkHasSubTitle,
    getFrameworkTitle,
    getFrameworkSubtitle,
    convertUnixTimeInMsToDateString,
    /**
     * Navigates to the company view page
     * @param companyId Dataland companyId
     * @param framework Dataland framework
     * @returns the promise of the router push action
     */
    goToResolveDataRequestPage(companyId: string, framework: DataTypeEnum) {
      const url = `/companies/${companyId}/frameworks/${framework}`;
      return this.$router.push(url);
    },
    /**
     * Navigates to the bulk data request page
     * @returns the promise of the router push action
     */
    goToBulkDataRequestPage() {
      const url = `/bulkdatarequest`;
      return this.$router.push(url);
    },

    /**
     * Gets list with all available frameworks
     * @returns array of frameworkSelectableItem
     */
    retrieveAvailableFrameworks(): Array<FrameworkSelectableItem> {
      return FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
        let displayName = humanizeStringOrNumber(dataTypeEnum);
        const frameworkDefinition = getFrontendFrameworkDefinition(dataTypeEnum);
        if (frameworkDefinition) {
          displayName = frameworkDefinition.label;
        }
        return {
          frameworkDataType: dataTypeEnum,
          displayName: displayName,
          disabled: false,
        };
      });
    },
    /**
     * Gets list of storedDataRequests
     */
    async getStoredRequestDataList() {
      this.waitingForData = true;
      this.storedDataRequests = [];
      try {
        if (this.getKeycloakPromise) {
          this.storedDataRequests = (
            await new ApiClientProvider(
              this.getKeycloakPromise()
            ).apiClients.requestController.getDataRequestsForRequestingUser()
          ).data;
        }
      } catch (error) {
        console.error(error);
      }
      this.waitingForData = false;
    },
    /**
     * Navigates to the view dataRequest page
     * @param event contains column that was clicked
     * @param event.data extended stored data request
     * @param event.originalEvent needed to get the clicked cell
     * @returns the promise of the router push action
     */
    onRowClick(event: DataTableRowClickEvent) {
      const clickedElement = event.originalEvent.target as HTMLElement;
      const isResolveButtonClick = clickedElement.id === 'resolveButton';
      if (!isResolveButtonClick) {
        const requestIdOfClickedRow = event.data.dataRequestId;
        return this.$router.push(`/requests/${requestIdOfClickedRow}`);
      }
    },
    /**
     * Sorts the list of storedDataRequests
     * @param event contains column to sort and sortOrder
     */
    onSort(event: DataTableSortEvent) {
      this.sortField = event.sortField as keyof ExtendedStoredDataRequest;
      this.sortOrder = event.sortOrder ?? 1;
      this.updateCurrentDisplayedData();
    },

    /**
     * Filterfunction for frameworks
     * @param framework dataland framework
     * @returns checks if given framework is selected
     */
    filterFramework(framework: string) {
      for (const selectedFramework of this.selectedFrameworks) {
        if (framework == selectedFramework.frameworkDataType) return true;
      }
      return false;
    },
    /**
     * Filterfunction for searchbar
     * @param companyName dataland companyName
     * @returns checks if given companyName contains searchbar text
     */
    filterSearchInput(companyName: string) {
      const lowerCaseCompanyName = (companyName ?? '').toLowerCase();
      const lowerCaseSearchString = this.searchBarInputFilter.toLowerCase();
      return lowerCaseCompanyName.includes(lowerCaseSearchString);
    },
    /**
     * Resets selected frameworks and searchBarInput
     */
    resetFilterAndSearchBar() {
      this.selectedFrameworks = this.availableFrameworks;
      this.searchBarInput = '';
    },
    /**
     * Updates the displayedData
     */
    updateCurrentDisplayedData() {
      this.displayedData = this.storedDataRequests
        .filter((dataRequest) => this.filterSearchInput(dataRequest.companyName))
        .filter((dataRequest) => this.filterFramework(dataRequest.dataType));
      this.displayedData.sort((a, b) => this.customCompareForExtendedStoredDataRequests(a, b));
      this.numberOfFilteredRequests = this.displayedData.length;
      this.displayedData = this.displayedData.slice(
        this.datasetsPerPage * this.currentPage,
        this.datasetsPerPage * (1 + this.currentPage)
      );
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      });
    },
    /**
     * Compares two extended stored data requests (sort field, request status, last modified, company name)
     * @param a ExtendedStoredDataRequest to sort
     * @param b ExtendedStoredDataRequest to sort
     * @returns result of the comparison
     */
    customCompareForExtendedStoredDataRequests(a: ExtendedStoredDataRequest, b: ExtendedStoredDataRequest) {
      const aValue = a[this.sortField];
      const bValue = b[this.sortField];

      if (this.sortField != ('requestStatus' as keyof ExtendedStoredDataRequest)) {
        if (aValue < bValue) return -1 * this.sortOrder;
        if (aValue > bValue) return this.sortOrder;
      }

      if (a.requestStatus != b.requestStatus)
        return this.customCompareForRequestStatus(a.requestStatus, b.requestStatus);

      if (a.lastModifiedDate < b.lastModifiedDate) return this.sortOrder;
      if (a.lastModifiedDate > b.lastModifiedDate) return -1 * this.sortOrder;

      if (a.companyName < b.companyName) return -1 * this.sortOrder;
      else return this.sortOrder;
    },
    /**
     * Compares two request status
     * @param a RequestStatus to compare
     * @param b RequestStatus to compare
     * @returns result of the comparison
     */
    customCompareForRequestStatus(a: RequestStatus, b: RequestStatus) {
      const sortOrderRequestStatus: { [key: string]: number } = {};
      sortOrderRequestStatus[RequestStatus.Answered] = 1;
      sortOrderRequestStatus[RequestStatus.Open] = 2;
      sortOrderRequestStatus[RequestStatus.Resolved] = 3;
      sortOrderRequestStatus[RequestStatus.Closed] = 4;
      sortOrderRequestStatus[RequestStatus.Withdrawn] = 5;
      if (sortOrderRequestStatus[a] <= sortOrderRequestStatus[b]) return -1 * this.sortOrder;
      return this.sortOrder;
    },
    /**
     * Updates the data for the current page
     * @param event event containing the new page
     */
    onPage(event: DataTablePageEvent) {
      this.currentPage = event.page;
      this.updateCurrentDisplayedData();
    },
  },
});
</script>
<style scoped>
#my-data-requests-overview-table tr:hover {
  cursor: pointer;
}
</style>
