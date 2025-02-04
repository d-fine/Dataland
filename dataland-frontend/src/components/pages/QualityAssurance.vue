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
                  placeholder="Search by company name"
                  class="w-12 pl-6 pr-6"
                />
              </span>
              <span class="w-3 p-input-icon-left" style="margin: 15px">
                <Calendar
                  data-test="reportingPeriod"
                  v-model="availableReportingPeriods"
                  placeholder="Search by reporting period"
                  :showIcon="true"
                  :manualInput="false"
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
                <span>{{ numberOfUnreviewedDatasets }}</span>
              </div>
            </span>
            <div class="pb-2 ml-3 flex justify-content-start">
              <span class="red-text" v-if="showNotEnoughCharactersWarning">Please type at least 3 characters</span>
            </div>
          </div>

          <div class="col-12 text-left p-3">
            <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
              <p class="font-medium text-xl">Loading data to be reviewed...</p>
              <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
            </div>

            <div class="card">
              <DataTable
                v-show="!waitingForData && displayDataOfPage.length > 0"
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
                :total-records="totalRecords"
                @page="onPage($event)"
              >
                <Column header="DATA ID" class="d-bg-white w-2 qa-review-id">
                  <template #body="slotProps">
                    {{ slotProps.data.dataId }}
                  </template>
                </Column>
                <Column header="COMPANY NAME" class="d-bg-white w-2 qa-review-company-name">
                  <template #body="slotProps">
                    {{ slotProps.data.companyName }}
                  </template>
                </Column>
                <Column header="FRAMEWORK" class="d-bg-white w-2 qa-review-framework">
                  <template #body="slotProps">
                    {{ humanizeString(slotProps.data.framework) }}
                  </template>
                </Column>
                <Column header="REPORTING PERIOD" class="d-bg-white w-2 qa-review-reporting-period">
                  <template #body="slotProps">
                    {{ slotProps.data.reportingPeriod }}
                  </template>
                </Column>
                <Column header="SUBMISSION DATE" class="d-bg-white w-2 qa-review-submission-date">
                  <template #body="slotProps">
                    {{ convertUnixTimeInMsToDateString(slotProps.data.timestamp) }}
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
              <div v-if="!waitingForData && displayDataOfPage.length == 0">
                <div class="d-center-div text-center px-7 py-4">
                  <p class="font-medium text-xl">There are no unreviewed datasets on Dataland matching your filters.</p>
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
import { ApiClientProvider } from '@/services/ApiClients';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
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
import type Keycloak from 'keycloak-js';
import { type GetInfoOnPendingDatasetsDataTypesEnum, type QaReviewResponse } from '@clients/qaservice';
import router from '@/router';
import { type DataTypeEnum } from '@clients/backend';
import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles.ts';

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
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      apiClientProvider: new ApiClientProvider(this.getKeycloakPromise()),
      displayDataOfPage: [] as QaReviewResponse[],
      waitingForData: true,
      KEYCLOAK_ROLE_REVIEWER,
      currentChunkIndex: 0,
      firstRowIndex: 0,
      totalRecords: 0,
      footerContent,
      debounceInMs: 300,
      timerId: 0,
      searchBarInput: '',
      selectedFrameworks: [] as Array<FrameworkSelectableItem>,
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      availableReportingPeriods: undefined as undefined | Array<Date>,
      notEnoughCharactersWarningTimeoutId: 0,
      showNotEnoughCharactersWarning: false,
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
        void this.getQaDataForCurrentPage();
      }
    },
    availableReportingPeriods() {
      this.currentChunkIndex = 0;
      this.firstRowIndex = 0;
      if (!this.waitingForData) {
        void this.getQaDataForCurrentPage();
      }
    },
    searchBarInput() {
      const isValid = this.validateSearchBarInput();
      if (isValid) {
        this.currentChunkIndex = 0;
        this.firstRowIndex = 0;
        if (this.timerId) {
          clearTimeout(this.timerId);
        }
        this.timerId = setTimeout(() => this.getQaDataForCurrentPage(), this.debounceInMs);
      }
    },
  },
  methods: {
    convertUnixTimeInMsToDateString,
    humanizeString: humanizeStringOrNumber,
    /**
     * Tells the typescript compiler to handle the DataTypeEnum input as type GetInfoOnUnreviewedDatasetsDataTypesEnum.
     * This is acceptable because both enums share the same origin (DataTypeEnum in backend).
     * @param input is a value with type DataTypeEnum
     * @returns GetInfoOnUnreviewedDatasetsDataTypesEnum
     */
    manuallyChangeTypeOfDataTypeEnum(input: DataTypeEnum): GetInfoOnPendingDatasetsDataTypesEnum {
      return input as GetInfoOnPendingDatasetsDataTypesEnum;
    },
    /**
     * Uses the dataland QA API to retrieve the information that is displayed on the quality assurance page
     */
    async getQaDataForCurrentPage() {
      try {
        this.waitingForData = true;
        this.displayDataOfPage = [];

        const selectedFrameworksAsSet = new Set<GetInfoOnPendingDatasetsDataTypesEnum>(
          this.selectedFrameworks.map((selectableItem) =>
            this.manuallyChangeTypeOfDataTypeEnum(selectableItem.frameworkDataType)
          )
        );
        const reportingPeriodFilter: Set<string> = new Set<string>(
          this.availableReportingPeriods?.map((date) => date.getFullYear().toString())
        );
        const companyNameFilter = this.searchBarInput === '' ? undefined : this.searchBarInput;
        const response = await this.apiClientProvider.apiClients.qaController.getInfoOnPendingDatasets(
          selectedFrameworksAsSet,
          reportingPeriodFilter,
          companyNameFilter,
          this.datasetsPerPage,
          this.currentChunkIndex
        );
        this.displayDataOfPage = response.data;
        this.totalRecords = (
          await this.apiClientProvider.apiClients.qaController.getNumberOfPendingDatasets(
            selectedFrameworksAsSet,
            reportingPeriodFilter,
            companyNameFilter
          )
        ).data;
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Navigates to the view framework data page on a click on the row of the company
     * @param event the row click event
     * @returns the promise of the router push action
     */
    goToQaViewPage(event: DataTableRowClickEvent) {
      const qaDataObject = event.data as QaReviewResponse;
      const qaUri = `/companies/${qaDataObject.companyId}/frameworks/${qaDataObject.framework}/${qaDataObject.dataId}`;
      return router.push(qaUri);
    },

    /**
     * Resets selected frameworks and searchBarInput
     */
    resetFilterAndSearchBar() {
      this.currentChunkIndex = 0;
      this.selectedFrameworks = [];
      this.availableReportingPeriods = [];
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
        this.firstRowIndex = this.currentChunkIndex * this.datasetsPerPage;
        void this.getQaDataForCurrentPage();
      }
    },
    /**
     * Validates the current company name search bar input.
     * If there are only one or two characters typed, an error message shall be rendered asking the user to
     * provide at least three characters.
     * @returns the outcome of the validation
     */
    validateSearchBarInput(): boolean {
      clearTimeout(this.notEnoughCharactersWarningTimeoutId);

      const inputLength = this.searchBarInput.length;
      const notEnoughCharacters = inputLength > 0 && inputLength < 3;

      if (notEnoughCharacters) {
        this.notEnoughCharactersWarningTimeoutId = setTimeout(() => {
          this.showNotEnoughCharactersWarning = true;
        }, 1000);
        return false;
      }

      this.showNotEnoughCharactersWarning = false;
      return true;
    },
  },
  computed: {
    numberOfUnreviewedDatasets(): string {
      if (!this.waitingForData) {
        if (this.totalRecords === 0) {
          return 'No results for this search.';
        } else {
          const startIndex = this.currentChunkIndex * this.datasetsPerPage + 1;
          const endIndex = Math.min(startIndex + this.datasetsPerPage - 1, this.totalRecords);
          return `Showing results ${startIndex}-${endIndex} of ${this.totalRecords}.`;
        }
      }
      return '';
    },
  },
});
</script>

<style>
#qa-data-result tr:hover {
  cursor: pointer;
}
</style>
