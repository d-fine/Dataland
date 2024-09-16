<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="2">
      <TheContent class="min-h-screen paper-section relative">
        <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_REVIEWER">
          <div
            id="searchBarAndFiltersContainer"
            class="w-full bg-white pt-4 justify-between"
            ref="searchBarAndFiltersContainer"
          >
            <span class="align-content-start flex items-center justify-start">
              <span class="w-3 p-input-icon-left" style="margin: 15px">
                <i class="pi pi-search pl-3 pr-3" aria-hidden="true" style="color: #958d7c" />
                <InputText
                  data-test="companyNameSearchbar"
                  v-model="searchBarInput"
                  placeholder="Search by Company Name"
                  class="w-12 pl-6 pr-6"
                />
              </span>
              <span class="w-3 p-input-icon-left" style="margin: 15px">
                <i class="pi pi-search pl-3 pr-3" aria-hidden="true" style="color: #958d7c" />
                <Calendar
                  data-test="reportingPeriod"
                  v-model="availableReportingPeriods"
                  placeholder="Search by Reporting Period"
                  :showIcon="true"
                  view="year"
                  dateFormat="yy"
                  selectionMode="multiple"
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

          <div class="col-12 text-left p-3">
            <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
              <p class="font-medium text-xl">Loading data to be reviewed...</p>
              <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
            </div>

            <div class="card">
              <DataTable
                v-show="!waitingForData && dataIdList.length > 0"
                :value="displayDataOfPage"
                class="table-cursor"
                id="qa-data-result"
                :rowHover="true"
                :first="firstRowIndex"
                data-test="qa-review-section"
                @row-click="goToQaViewPage($event)"
                paginator
                paginator-position="top"
                :rows="datasetsPerPage"
                lazy
                :total-records="dataIdList.length"
                @page="onPage($event)"
              >
                <Column header="DATA ID" class="d-bg-white w-2 qa-review-id">
                  <template #body="slotProps">
                    {{ slotProps.data.dataId }}
                  </template>
                </Column>
                <Column header="COMPANY NAME" class="d-bg-white w-2 qa-review-company-name">
                  <template #body="slotProps">
                    {{ slotProps.data.companyInformation.companyName }}
                  </template>
                </Column>
                <Column header="FRAMEWORK" class="d-bg-white w-2 qa-review-framework">
                  <template #body="slotProps">
                    {{ humanizeString(slotProps.data.metaInformation.dataType) }}
                  </template>
                </Column>
                <Column header="REPORTING PERIOD" class="d-bg-white w-2 qa-review-reporting-period">
                  <template #body="slotProps">
                    {{ slotProps.data.metaInformation.reportingPeriod }}
                  </template>
                </Column>
                <Column header="SUBMISSION DATE" class="d-bg-white w-2 qa-review-submission-date">
                  <template #body="slotProps">
                    {{ convertUnixTimeInMsToDateString(slotProps.data.metaInformation.uploadTime) }}
                  </template>
                </Column>
                <Column field="reviewDataset" header="" class="w-2 d-bg-white qa-review-button">
                  <template #body>
                    <div class="text-right text-primary no-underline font-bold">
                      <span>REVIEW</span>
                      <span class="ml-3">></span>
                    </div>
                  </template>
                </Column>
              </DataTable>
              <div v-if="!waitingForData && dataIdList.length == 0">
                <div class="d-center-div text-center px-7 py-4">
                  <p class="font-medium text-xl">There are no data requests on Dataland matching your filters.</p>
                </div>
              </div>
            </div>
          </div>
        </AuthorizationWrapper>
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
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { defineComponent, inject } from 'vue';
import { type CompanyInformation, type DataMetaInformation } from '@clients/backend';
import { type ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakUtils';
import DataTable, { type DataTablePageEvent, type DataTableRowClickEvent } from 'primevue/datatable';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import Column from 'primevue/column';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { type FrameworkSelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { retrieveAvailableFrameworks } from '@/utils/RequestsOverviewPageUtils';
import InputText from 'primevue/inputtext';
import Calendar from 'primevue/calendar';

export default defineComponent({
  name: 'QualityAssurance',
  components: {
    DatasetsTabMenu,
    AuthorizationWrapper,
    TheFooter,
    TheContent,
    TheHeader,
    AuthenticationWrapper,
    FrameworkDataSearchDropdownFilter,
    DataTable,
    Column,
    InputText,
    Calendar,
  },
  setup() {
    return {
      datasetsPerPage: 10,
      apiClientProvider: inject<ApiClientProvider>('apiClientProvider'),
    };
  },
  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      dataIdList: [] as Array<string>,
      displayDataOfPage: [] as QaDataObject[],
      waitingForData: true,
      KEYCLOAK_ROLE_REVIEWER,
      metaInformation: null as DataMetaInformation | null,
      companyInformation: null as CompanyInformation | null,
      currentChunkIndex: 0,
      firstRowIndex: 0,
      footerContent,
      debounceInMs: 300,
      timerId: 0,
      searchBarInput: '',
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      availableReportingPeriods: undefined as undefined | Date,
      reportingPeriod: undefined as undefined | Date,
    };
  },
  mounted() {
    this.getQaDataForCurrentPage().catch((error) => console.log(error));
    this.availableFrameworks = retrieveAvailableFrameworks();
  },
  watch: {
    selectedFrameworks() {
      this.currentChunkIndex = 0;
      this.firstRowIndex = 0;
      if (!this.waitingForData) {
        this.getQaDataForCurrentPage();
      }
    },
    selectedRequestStatus() {
      this.currentChunkIndex = 0;
      this.firstRowIndex = 0;
      if (!this.waitingForData) {
        this.getQaDataForCurrentPage();
      }
    },
    searchBarInput(newSearch: string) {
      this.searchBarInput = newSearch;
      this.currentChunkIndex = 0;
      this.firstRowIndex = 0;
      if (this.timerId) {
        clearTimeout(this.timerId);
      }
      this.timerId = setTimeout(() => this.getQaDataForCurrentPage(), this.debounceInMs);
    },
  },
  methods: {
    convertUnixTimeInMsToDateString,
    humanizeString: humanizeStringOrNumber,
    /**
     * Uses the dataland API to build the QaDataObject which is displayed on the quality assurance page
     */
    async getQaDataForCurrentPage() {
      try {
        this.waitingForData = true;
        this.displayDataOfPage = [];
        const dataOfPage = [] as QaDataObject[];
        const response = await assertDefined(this.apiClientProvider).apiClients.qaController.getUnreviewedDatasetsIds();
        this.dataIdList = response.data;
        const firstDatasetOnPageIndex = this.currentChunkIndex * this.datasetsPerPage;
        const dataIdsOnPage = this.dataIdList.slice(
          firstDatasetOnPageIndex,
          firstDatasetOnPageIndex + this.datasetsPerPage
        );
        for (const dataId of dataIdsOnPage) {
          dataOfPage.push(await this.addDatasetAssociatedInformationToDisplayList(dataId));
        }
        this.displayDataOfPage = dataOfPage;
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Gathers meta and company information associated with a dataset if the information can be retrieved
     * @param dataId the ID of the corresponding dataset
     * @returns a promise on the fetched data object
     */
    async addDatasetAssociatedInformationToDisplayList(dataId: string): Promise<QaDataObject> {
      const metaDataResponse = await assertDefined(
        this.apiClientProvider
      ).backendClients.metaDataController.getDataMetaInfo(dataId);
      this.metaInformation = metaDataResponse.data;
      const companyResponse = await assertDefined(
        this.apiClientProvider
      ).backendClients.companyDataController.getCompanyById(this.metaInformation.companyId);
      this.companyInformation = companyResponse.data.companyInformation;
      return {
        dataId: dataId,
        metaInformation: this.metaInformation,
        companyInformation: this.companyInformation,
      };
    },
    /**
     * Navigates to the view framework data page on a click on the row of the company
     * @param event the row click event
     * @returns the promise of the router push action
     */
    goToQaViewPage(event: DataTableRowClickEvent) {
      const qaDataObject = event.data as QaDataObject;
      const qaUri = `/companies/${qaDataObject.metaInformation.companyId}/frameworks/${qaDataObject.metaInformation.dataType}/${qaDataObject.dataId}`;
      return this.$router.push(qaUri);
    },

    /**
     * Resets selected frameworks and searchBarInput
     */
    resetFilterAndSearchBar() {
      this.currentChunkIndex = 0;
      this.selectedFrameworks = [];
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
        this.getQaDataForCurrentPage();
      }
    },
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
});
interface QaDataObject {
  dataId: string;
  metaInformation: DataMetaInformation;
  companyInformation: CompanyInformation;
}
</script>

<style>
#qa-data-result tr:hover {
  cursor: pointer;
}
</style>
