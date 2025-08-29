<template>
  <DatasetsTabMenu :initialTabIndex="3">
    <TheContent class="min-h-screen relative">
      <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_REVIEWER">
        <div class="container">
          <div class="company-search" data-test="companySearchBarWithMessage">
            <IconField id="company-search-bar">
              <InputIcon class="pi pi-search" />
              <InputText
                data-test="companyNameSearchbar"
                v-model="searchBarInput"
                placeholder="Search by company name"
                fluid
                variant="filled"
              />
            </IconField>
            <Message severity="error" variant="simple" size="small" v-if="showNotEnoughCharactersWarning">
              Please type at least 3 characters
            </Message>
          </div>

          <DatePicker
            class="search-filter"
            data-test="reportingPeriod"
            v-model="availableReportingPeriods"
            placeholder="Search by reporting period"
            :showIcon="true"
            :manualInput="false"
            view="year"
            dateFormat="yy"
            selectionMode="multiple"
          />

          <FrameworkDataSearchDropdownFilter
            v-model="selectedFrameworks"
            class="search-filter"
            :available-items="availableFrameworks"
            filter-name="Framework"
            data-test="framework-picker"
            id="framework-filter"
            filter-placeholder="Search by Frameworks"
            :max-selected-labels="1"
            selected-items-label="{0} frameworks selected"
          />

          <PrimeButton variant="link" @click="resetFilterAndSearchBar" label="RESET" />
          <Message
            class="info-message"
            variant="simple"
            severity="secondary"
            data-test="showingNumberOfUnreviewedDatasets"
            >{{ numberOfUnreviewedDatasets }}</Message
          >
        </div>

        <div class="col-12 text-left p-3">
          <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
            <p class="font-medium text-xl">Loading data to be reviewed...</p>
            <DatalandProgressSpinner />
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
              <Column header="DATA ID" class="w-2">
                <template #body="slotProps">
                  {{ slotProps.data.dataId }}
                </template>
              </Column>
              <Column header="COMPANY NAME" class="w-2">
                <template #body="slotProps">
                  <span data-test="qa-review-company-name">{{ slotProps.data.companyName }}</span>
                </template>
              </Column>
              <Column header="FRAMEWORK" class="w-2">
                <template #body="slotProps">
                  {{ humanizeString(slotProps.data.framework) }}
                </template>
              </Column>
              <Column header="REPORTING PERIOD" class="w-2">
                <template #body="slotProps">
                  {{ slotProps.data.reportingPeriod }}
                </template>
              </Column>
              <Column header="SUBMISSION DATE" class="w-2">
                <template #body="slotProps">
                  {{ convertUnixTimeInMsToDateString(slotProps.data.timestamp) }}
                </template>
              </Column>
              <Column field="reviewDataset" header="" class="w-2 qa-review-button">
                <template #body="slotProps">
                  <PrimeButton
                    @click="goToQaViewPageByButton(slotProps.data)"
                    label="REVIEW"
                    icon="pi pi-chevron-right"
                    icon-pos="right"
                    variant="link"
                  />
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

  <TheFooter />
</template>

<script lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { type FrameworkSelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';
import { retrieveAvailableFrameworks } from '@/utils/RequestsOverviewPageUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DataTypeEnum } from '@clients/backend';
import { type GetInfoOnDatasetsDataTypesEnum, type QaReviewResponse } from '@clients/qaservice';
import type Keycloak from 'keycloak-js';
import DatePicker from 'primevue/datepicker';
import Column from 'primevue/column';
import DataTable, { type DataTablePageEvent, type DataTableRowClickEvent } from 'primevue/datatable';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import PrimeButton from 'primevue/button';
import Message from 'primevue/message';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'QualityAssurance',
  components: {
    DatalandProgressSpinner,
    DatasetsTabMenu,
    AuthorizationWrapper,
    TheFooter,
    TheContent,
    FrameworkDataSearchDropdownFilter,
    DataTable,
    Column,
    InputText,
    InputIcon,
    IconField,
    PrimeButton,
    DatePicker,
    Message,
  },
  setup() {
    return {
      datasetsPerPage: 10,
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    return {
      apiClientProvider: new ApiClientProvider(this.getKeycloakPromise()),
      displayDataOfPage: [] as QaReviewResponse[],
      waitingForData: true,
      KEYCLOAK_ROLE_REVIEWER,
      currentChunkIndex: 0,
      firstRowIndex: 0,
      totalRecords: 0,
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
    manuallyChangeTypeOfDataTypeEnum(input: DataTypeEnum): GetInfoOnDatasetsDataTypesEnum {
      return input as GetInfoOnDatasetsDataTypesEnum;
    },
    /**
     * Uses the dataland QA API to retrieve the information that is displayed on the quality assurance page
     */
    async getQaDataForCurrentPage() {
      try {
        this.waitingForData = true;
        this.displayDataOfPage = [];

        const selectedFrameworksAsSet = new Set<GetInfoOnDatasetsDataTypesEnum>(
          this.selectedFrameworks.map((selectableItem) =>
            this.manuallyChangeTypeOfDataTypeEnum(selectableItem.frameworkDataType)
          )
        );
        const reportingPeriodFilter: Set<string> = new Set<string>(
          this.availableReportingPeriods?.map((date) => date.getFullYear().toString())
        );
        const companyNameFilter = this.searchBarInput === '' ? undefined : this.searchBarInput;
        const response = await this.apiClientProvider.apiClients.qaController.getInfoOnDatasets(
          selectedFrameworksAsSet,
          reportingPeriodFilter,
          companyNameFilter,
          undefined,
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
     * Navigates to the view framework data page on a click on the row of the company
     * @param qaDataObject stored information about the row
     */
    goToQaViewPageByButton(qaDataObject: QaReviewResponse): void {
      const qaUri = `/companies/${qaDataObject.companyId}/frameworks/${qaDataObject.framework}/${qaDataObject.dataId}`;
      void router.push(qaUri);
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

<style scoped>
.container {
  margin: 0;
  width: 100%;
  padding: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-lg);
  align-items: start;

  .company-search {
    display: flex;
    flex-direction: column;
    width: 30%;
  }

  .search-filter {
    width: 15%;
    text-align: left;
  }

  .info-message:last-child {
    margin-left: auto;
    margin-top: var(--spacing-xs);
  }
}

#qa-data-result tr,
.table-cursor {
  cursor: pointer;
}

.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}

.qa-review-button {
  text-align: end;
}
</style>
