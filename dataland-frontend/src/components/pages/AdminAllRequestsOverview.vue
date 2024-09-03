<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="5">
      <TheContent class="min-h-screen paper-section relative">
        <div v-if="waitingForData || currentDataRequests.length > 0">
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
                  placeholder="Search by Requester"
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
                filter-placeholder="Search by Frameworks"
                class="ml-3"
                style="margin: 15px"
              />
              <FrameworkDataSearchDropdownFilter
                v-model="selectedRequestStatus"
                ref="frameworkFilter"
                :available-items="availableRequestStatus"
                filter-name="Request Status"
                data-test="requested-Datasets-frameworks"
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
            </span>
          </div>
          <div class="col-12 text-left p-3">
            <div class="card">
              <DataTable
                v-if="currentDataRequests && currentDataRequests.length > 0"
                ref="dataTable"
                :value="currentDataRequests"
                :paginator="true"
                @page="onPage($event)"
                :lazy="true"
                :first="previousRecords"
                :total-records="totalRecords"
                :rows="rowsPerPage"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                :alwaysShowPaginator="false"
                currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
                @row-click="onRowClick($event)"
                @page-update="handlePageUpdate"
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
            </div>
          </div>
        </div>
        <div v-if="!waitingForData && currentDataRequests.length == 0">
          <div class="d-center-div text-center px-7 py-4">
            <p class="font-medium text-xl">Currently, there are no data requests on Dataland.</p>
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
import { type ExtendedStoredDataRequest } from '@clients/communitymanager';
import InputText from 'primevue/inputtext';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import type { FrameworkSelectableItem, SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { accessStatusBadgeClass, badgeClass } from '@/utils/RequestUtils';
import FrameworkDataSearchResults from '@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue';
import { retrieveAvailableFrameworks, retrieveAvailableRequestStatus } from '@/utils/RequestsOverviewPageUtils';

export default defineComponent({
  name: 'MyDataRequestsOverview',
  computed: {},
  components: {
    // FrameworkDataSearchResults,
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
      totalRecords: 0,
      rowsPerPage: 100,
      previousRecords: 0,
      currentDataRequests: [] as ExtendedStoredDataRequest[],
      footerContent,
      searchBarInput: '',
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      availableRequestStatus: [] as Array<SelectableItem>,
      selectedRequestStatus: [] as Array<SelectableItem>,
    };
  },
  mounted() {
    this.availableFrameworks = retrieveAvailableFrameworks();
    this.availableRequestStatus = retrieveAvailableRequestStatus();
    this.getStoredRequestDataList().catch((error) => console.error(error));
    this.resetFilterAndSearchBar();
  },
  watch: {
    selectedFrameworks() {
      const frameworksList = this.selectedFrameworks.map((selectableItem) => selectableItem.frameworkDataType);
      console.log(frameworksList);
    },
    selectedRequestStatus() {
      const requestStatusList = this.selectedRequestStatus.map((selectableItem) => selectableItem.displayName);
      console.log(requestStatusList);
    },
    searchBarInput(newSearch: string) {
      this.searchBarInput = newSearch;
      console.log(this.searchBarInput);
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
    async getStoredRequestDataList() {
      this.waitingForData = true;
      this.currentDataRequests = [];
      try {
        if (this.getKeycloakPromise) {
          this.currentDataRequests = (
            await new ApiClientProvider(this.getKeycloakPromise()).apiClients.requestController.getDataRequests(
              undefined,
              undefined,
              undefined,
              undefined,
              undefined,
              undefined,
              this.datasetsPerPage,
              this.currentPage
            )
          ).data;
        }
      } catch (error) {
        console.error(error);
      }
      this.waitingForData = false;
    },

    /* /!**
     * Gets list of storedDataRequests
     *!/
    async fetchDataFromBackend(frameworks: Set<DataTypeEnum> | undefined, requestStatuses: Set<String> | undefined) {
      this.waitingForData = true;
      this.currentDataRequests = [];
      try {
        if (this.getKeycloakPromise) {
          this.currentDataRequests = (
            await new ApiClientProvider(
              this.getKeycloakPromise()
            ).apiClients.requestController.getDataRequests(
                frameworks,
                undefined,
                requestStatuses,
                undefined,
                undefined,
                undefined,
                this.datasetsPerPage,
                this.currentPage
            )
          ).data;
        }
      } catch (error) {
        console.error(error);
      }
      this.waitingForData = false;
    },*/

    /**
     * Resets selected frameworks and searchBarInput
     */
    resetFilterAndSearchBar() {
      this.selectedFrameworks = [];
      this.selectedRequestStatus = [];
      this.searchBarInput = '';
    },

    /**
     * Updates the current page.
     * An update of the currentPage automatically triggers a data Update
     * @param pageNumber the new page index
     */
    handlePageUpdate(pageNumber: number) {
      if (pageNumber != this.currentPage) {
        this.waitingForData = true;
        this.currentPage = pageNumber;
        this.previousRecords = this.currentPage * this.rowsPerPage;
      }
    },
    /**
     * Updates the current Page in the parent component
     * @param event DataTablePageEvent
     */
    onPage(event: DataTablePageEvent) {
      window.scrollTo(0, 0);
      this.$emit('page-update', event.page);
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
  },
});
</script>
