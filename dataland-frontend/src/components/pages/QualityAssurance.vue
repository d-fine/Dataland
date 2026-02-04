<template>
  <TheContent class="min-h-screen relative">
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_REVIEWER">
      <div class="container">
        <div class="company-search" data-test="companySearchBarWithMessage">
          <IconField id="company-search-bar">
            <InputIcon class="pi pi-search" />
            <InputText
              data-test="companyNameSearchbar"
              v-model="searchBarInput"
              placeholder="Search by Company Name or Identifier"
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
          :updateModelType="'date'"
          placeholder="Reporting Period"
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
            <Column field="reviewDataset" header="REVIEW AVAILABLE" class="w-2 qa-review-button">
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
</template>

<script setup lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import TheContent from '@/components/generics/TheContent.vue';
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
import { computed, inject, onMounted, ref, watch } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

const datasetsPerPage = 10;
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const displayDataOfPage = ref<QaReviewResponse[]>([]);
const waitingForData = ref(true);
const currentChunkIndex = ref(0);
const firstRowIndex = ref(0);
const totalRecords = ref(0);
const searchBarInput = ref('');
const selectedFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const availableFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const availableReportingPeriods = ref<undefined | Array<Date>>(undefined);
const showNotEnoughCharactersWarning = ref(false);

const debounceInMs = 300;
let timerId = 0;
let notEnoughCharactersWarningTimeoutId = 0;

const humanizeString = humanizeStringOrNumber;

/**
 * Tells the typescript compiler to handle the DataTypeEnum input as type GetInfoOnUnreviewedDatasetsDataTypesEnum.
 * This is acceptable because both enums share the same origin (DataTypeEnum in backend).
 * @param input is a value with type DataTypeEnum
 * @returns GetInfoOnUnreviewedDatasetsDataTypesEnum
 */
function manuallyChangeTypeOfDataTypeEnum(input: DataTypeEnum): GetInfoOnDatasetsDataTypesEnum {
  return input as GetInfoOnDatasetsDataTypesEnum;
}

/**
 * Uses the dataland QA API to retrieve the information that is displayed on the quality assurance page
 */
async function getQaDataForCurrentPage(): Promise<void> {
  try {
    waitingForData.value = true;
    displayDataOfPage.value = [];

    const selectedFrameworksAsSet = new Set<GetInfoOnDatasetsDataTypesEnum>(
      selectedFrameworks.value.map((selectableItem) =>
        manuallyChangeTypeOfDataTypeEnum(selectableItem.frameworkDataType)
      )
    );
    const reportingPeriodFilter: Set<string> = new Set<string>(
      availableReportingPeriods.value?.map((date) => date.getFullYear().toString())
    );
    const companyNameFilter = searchBarInput.value === '' ? undefined : searchBarInput.value;
    const response = await apiClientProvider.apiClients.qaController.getInfoOnDatasets(
      selectedFrameworksAsSet,
      reportingPeriodFilter,
      companyNameFilter,
      undefined,
      datasetsPerPage,
      currentChunkIndex.value
    );
    displayDataOfPage.value = response.data;
    totalRecords.value = (
      await apiClientProvider.apiClients.qaController.getNumberOfPendingDatasets(
        selectedFrameworksAsSet,
        reportingPeriodFilter,
        companyNameFilter
      )
    ).data;
    waitingForData.value = false;
  } catch (error) {
    console.error(error);
  }
}

/**
 * Navigates to the view framework data page on a click on the row of the company
 * @param event the row click event
 * @returns the promise of the router push action
 */
function goToQaViewPage(event: DataTableRowClickEvent): ReturnType<typeof router.push> {
  const qaDataObject = event.data as QaReviewResponse;
  const qaUri = `/companies/${qaDataObject.companyId}/frameworks/${qaDataObject.framework}/${qaDataObject.dataId}`;
  return router.push(qaUri);
}

/**
 * Navigates to the view framework data page on a click on the row of the company
 * @param qaDataObject stored information about the row
 */
function goToQaViewPageByButton(qaDataObject: QaReviewResponse): void {
  const qaUri = `/companies/${qaDataObject.companyId}/frameworks/${qaDataObject.framework}/${qaDataObject.dataId}`;
  void router.push(qaUri);
}

/**
 * Resets selected frameworks and searchBarInput
 */
function resetFilterAndSearchBar(): void {
  currentChunkIndex.value = 0;
  selectedFrameworks.value = [];
  availableReportingPeriods.value = [];
  searchBarInput.value = '';
}

/**
 * Updates the current Page
 * @param event DataTablePageEvent
 */
function onPage(event: DataTablePageEvent): void {
  globalThis.scrollTo(0, 0);
  if (event.page != currentChunkIndex.value) {
    currentChunkIndex.value = event.page;
    firstRowIndex.value = currentChunkIndex.value * datasetsPerPage;
    void getQaDataForCurrentPage();
  }
}

/**
 * Validates the current company name search bar input.
 * If there are only one or two characters typed, an error message shall be rendered asking the user to
 * provide at least three characters.
 * @returns the outcome of the validation
 */
function validateSearchBarInput(): boolean {
  clearTimeout(notEnoughCharactersWarningTimeoutId);

  const inputLength = searchBarInput.value.length;
  const notEnoughCharacters = inputLength > 0 && inputLength < 3;

  if (notEnoughCharacters) {
    notEnoughCharactersWarningTimeoutId = setTimeout(() => {
      showNotEnoughCharactersWarning.value = true;
    }, 1000);
    return false;
  }

  showNotEnoughCharactersWarning.value = false;
  return true;
}

watch(selectedFrameworks, () => {
  currentChunkIndex.value = 0;
  firstRowIndex.value = 0;
  if (!waitingForData.value) {
    void getQaDataForCurrentPage();
  }
});

watch(availableReportingPeriods, () => {
  currentChunkIndex.value = 0;
  firstRowIndex.value = 0;
  if (!waitingForData.value) {
    void getQaDataForCurrentPage();
  }
});

watch(searchBarInput, () => {
  const isValid = validateSearchBarInput();
  if (isValid) {
    currentChunkIndex.value = 0;
    firstRowIndex.value = 0;
    if (timerId) {
      clearTimeout(timerId);
    }
    timerId = setTimeout(() => getQaDataForCurrentPage(), debounceInMs);
  }
});

const numberOfUnreviewedDatasets = computed((): string => {
  if (!waitingForData.value) {
    if (totalRecords.value === 0) {
      return 'No results for this search.';
    } else {
      const startIndex = currentChunkIndex.value * datasetsPerPage + 1;
      const endIndex = Math.min(startIndex + datasetsPerPage - 1, totalRecords.value);
      return `Showing results ${startIndex}-${endIndex} of ${totalRecords.value}.`;
    }
  }
  return '';
});

onMounted(() => {
  getQaDataForCurrentPage().catch((error) => console.log(error));
  availableFrameworks.value = retrieveAvailableFrameworks();
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
