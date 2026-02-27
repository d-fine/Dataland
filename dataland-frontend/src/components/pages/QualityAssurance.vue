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
          input-class="w-full"
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
          input-class="w-full"
          :available-items="availableFrameworks"
          filter-name="Framework"
          data-test="framework-picker"
          id="framework-filter"
          filter-placeholder="Search by Frameworks"
          :max-selected-labels="1"
          selected-items-label="{0} frameworks selected"
        />

        <PrimeButton variant="link" @click="resetFilterAndSearchBar" label="RESET" data-test="reset-filters-button" />
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
            @row-click="onRowClicked($event)"
            paginator
            paginator-position="top"
            :rows="datasetsPerPage"
            lazy
            :total-records="totalRecords"
            @page="onPage($event)"
          >
            <Column header="DATA ID">
              <template #body="slotProps">
                {{ slotProps.data.dataId }}
              </template>
            </Column>
            <Column header="COMPANY NAME">
              <template #body="slotProps">
                <span data-test="qa-review-company-name">{{ slotProps.data.companyName }}</span>
              </template>
            </Column>
            <Column header="FRAMEWORK">
              <template #body="slotProps">
                {{ humanizeStringOrNumber(slotProps.data.framework) }}
              </template>
            </Column>
            <Column header="REPORTING PERIOD">
              <template #body="slotProps">
                {{ slotProps.data.reportingPeriod }}
              </template>
            </Column>
            <Column header="SUBMISSION DATE">
              <template #body="slotProps">
                {{ convertUnixTimeInMsToDateString(slotProps.data.timestamp) }}
              </template>
            </Column>
            <Column header="PRIORITY" class="w-2">
              <template #body="slotProps">
                <DatalandTag
                  v-if="getPriorityForRow(slotProps.data) !== undefined"
                  class="dataland-tag"
                  :severity="dataSourcingPrioritySeverity(getPriorityForRow(slotProps.data)!)"
                  :value="String(getPriorityForRow(slotProps.data)!)"
                />
              </template>
            </Column>
            <Column header="NUMBER OF QA REPORTS">
              <template #body="slotProps">
                {{ slotProps.data.numberQaReports }}
              </template>
            </Column>
            <Column field="reviewDataset" header="REVIEW" class="qa-review-status-cell">
              <template #body="slotProps">
                <PrimeButton
                  v-if="
                    slotProps.data.reviewStatus === 'Start Review' || slotProps.data.reviewStatus === 'Continue Review'
                  "
                  @click.stop="handleRowAction(slotProps.data)"
                  class="qa-review-button"
                  data-test="goToReviewButton"
                  :label="slotProps.data.reviewStatus"
                  icon="pi pi-chevron-right"
                  icon-pos="right"
                  variant="link"
                />
                <span v-else>
                  {{ slotProps.data.reviewStatus }}
                </span>
              </template>
            </Column>
          </DataTable>
          <div v-if="!waitingForData && displayDataOfPage.length == 0">
            <div class="d-center-div text-center px-7 py-4">
              <p class="font-medium text-xl">There are no unreviewed datasets on Dataland matching your filters.</p>
            </div>
          </div>
        </div>
        <PrimeDialog
          v-model:visible="isConfirmationModalVisible"
          header="Start Review?"
          modal
          :dismissable-mask="true"
          style="min-width: 20rem; text-align: center"
          data-test="confirmation-modal"
          @hide="closeConfirmationModal"
        >
          <div style="text-align: center; padding: 8px 0">
            <div class="confirmation-modal-message">
              <div>Are you sure you want to start a review for this dataset?</div>
              <div>Once started, the review cannot be deleted and will be visible for other reviewers on Dataland.</div>
            </div>
          </div>
          <div v-if="errorMessage" data-test="confirmation-modal-error-message">
            <Message severity="error" class="my-3" style="max-width: 30rem; text-align: left">{{
              errorMessage
            }}</Message>
          </div>
          <template #footer>
            <PrimeButton
              label="CANCEL"
              @click="closeConfirmationModal"
              variant="outlined"
              data-test="cancel-confirmation-modal-button"
            />
            <PrimeButton label="CONFIRM" @click="confirmStartReview" data-test="ok-confirmation-modal-button" />
          </template>
        </PrimeDialog>
      </div>
    </AuthorizationWrapper>
  </TheContent>
</template>

<script setup lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import DatalandTag from '@/components/general/DatalandTag.vue';
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
import { type BasicDataDimensions } from '@clients/datasourcingservice';
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
import PrimeDialog from 'primevue/dialog';
import { computed, inject, onMounted, ref, watch } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { AxiosError } from 'axios';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter.ts';

const datasetsPerPage = 10;
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

type QaReviewRow = QaReviewResponse & { reviewStatus: string };
const displayDataOfPage = ref<QaReviewRow[]>([]);
const waitingForData = ref(true);
const currentChunkIndex = ref(0);
const firstRowIndex = ref(0);
const totalRecords = ref(0);
const searchBarInput = ref('');
const selectedFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const availableFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const availableReportingPeriods = ref<Array<Date>>([]);
const showNotEnoughCharactersWarning = ref(false);
const isConfirmationModalVisible = ref(false);
const selectedDataId = ref<string>('');
const errorMessage = ref<string>('');
const priorityByDimensions = ref<Record<string, number>>({});

const debounceInMs = 300;
let timerId = 0;
let notEnoughCharactersWarningTimeoutId = 0;

/**
 * Tells the TypeScript compiler to handle the DataTypeEnum input as type GetInfoOnUnreviewedDatasetsDataTypesEnum.
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
      availableReportingPeriods.value.map((date) => date.getFullYear().toString())
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
    displayDataOfPage.value = await Promise.all(
      response.data.map(async (row) => ({
        ...row,
        reviewStatus: await getReviewStatus(row.reviewerUserId, row.reviewerUserName),
      }))
    );
    totalRecords.value = (
      await apiClientProvider.apiClients.qaController.getNumberOfPendingDatasets(
        selectedFrameworksAsSet,
        reportingPeriodFilter,
        companyNameFilter
      )
    ).data;
    waitingForData.value = false;
    await fetchPriorities();
  } catch (error) {
    console.error(error);
  }
}

/**
 * Returns the DatalandTag severity string for the given numeric data sourcing priority.
 * @param priority the numeric priority value (1–10)
 */
function dataSourcingPrioritySeverity(priority: number): string {
  if (priority <= 3) return 'sourcing-priority-high';
  if (priority <= 6) return 'sourcing-priority-medium';
  if (priority <= 9) return 'sourcing-priority-low';
  return 'sourcing-priority-slate';
}

/**
 * Returns the priority for the given QA row, or undefined if none is available.
 * @param row the QA review response row
 */
function getPriorityForRow(row: QaReviewRow): number | undefined {
  return priorityByDimensions.value[`${row.companyId}|${row.framework}|${row.reportingPeriod}`];
}

/**
 * Fetches priorities for the currently displayed datasets and populates priorityByDimensions.
 */
async function fetchPriorities(): Promise<void> {
  try {
    const dimensions: BasicDataDimensions[] = displayDataOfPage.value.map((row) => ({
      companyId: row.companyId,
      dataType: row.framework,
      reportingPeriod: row.reportingPeriod,
    }));
    const priorityResponse =
      await apiClientProvider.apiClients.dataSourcingController.getDataSourcingPriorities(dimensions);
    const newPriorityByDimensions: Record<string, number> = {};
    priorityResponse.data.forEach((entry) => {
      newPriorityByDimensions[`${entry.companyId}|${entry.dataType}|${entry.reportingPeriod}`] = entry.priority;
    });
    priorityByDimensions.value = newPriorityByDimensions;
  } catch (error) {
    console.error(error);
  }
}

/**
 * Handles the click on a row in the QA table by calling the handleRowAction function with the data of the clicked row.
 * @param event is the DataTableRowClickEvent that is emitted when a row in the QA table is clicked. It contains the data of the clicked row.
 */
function onRowClicked(event: DataTableRowClickEvent): void {
  handleRowAction(event.data as QaReviewRow);
}

/**
 * Handles the click on a row in the QA table.
 * If the dataset of the clicked row has not been reviewed before, a confirmation modal will be opened to confirm the start of a new dataset review.
 * If the dataset already has an ongoing review, the user will be directly navigated to the corresponding dataset review page.
 */
function handleRowAction(qaDataObject: QaReviewRow): void {
  if (qaDataObject.datasetReviewId == null) {
    selectedDataId.value = qaDataObject.dataId;
    isConfirmationModalVisible.value = true;
  } else {
    void goToQaViewPage(qaDataObject.companyId, qaDataObject.framework, qaDataObject.dataId);
  }
}

/**
 * Navigates to the dataset review page for the dataset with the given dataId, companyId and framework.
 */
function goToQaViewPage(companyId: string, framework: string, dataId: string): ReturnType<typeof router.push> {
  const qaUri = `/companies/${companyId}/frameworks/${framework}/${dataId}`;
  return router.push(qaUri);
}

/**
 * Creates a dataset review for the dataset with the given dataId and navigates to the corresponding dataset review page.
 *
 */
async function createAndViewDatasetReview(dataId: string): Promise<void> {
  try {
    const response = await apiClientProvider.apiClients.datasetReviewController.postDatasetReview(dataId);
    await goToQaViewPage(response.data.companyId, response.data.dataType, dataId);
  } catch (error) {
    if (error instanceof AxiosError) {
      errorMessage.value = formatAxiosErrorMessage(error);
    } else {
      errorMessage.value = 'Failed to create dataset review.';
    }
    console.error(errorMessage.value);
  }
}

/**
 * Confirms the start of a dataset review in the confirmation modal.
 * Creates a dataset review for the dataset with the selected data id and navigates to the corresponding dataset review page.
 */
async function confirmStartReview(): Promise<void> {
  await createAndViewDatasetReview(selectedDataId.value);
  if (!errorMessage.value) {
    closeConfirmationModal();
  }
}

/**
 * Closes the confirmation modal and resets the selected data id and error message.
 */
function closeConfirmationModal(): void {
  isConfirmationModalVisible.value = false;
  selectedDataId.value = '';
  errorMessage.value = '';
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

/**
 * Determines the label of the review button in the table depending.
 * @param reviewerUserId the user id of the reviewer of the dataset
 * @param reviewerUserName the user name of the reviewer of the dataset
 * @returns the label of the review button
 */
async function getReviewStatus(
  reviewerUserId: string | undefined,
  reviewerUserName: string | undefined
): Promise<string> {
  const keycloak = await assertDefined(getKeycloakPromise)();
  const keycloakUserId = keycloak.idTokenParsed?.sub;
  if (reviewerUserId && reviewerUserName) {
    return keycloakUserId === reviewerUserId ? 'Continue Review' : reviewerUserName;
  }
  return 'Start Review';
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

.qa-review-status-cell {
  text-align: left;
  display: inline-flex;
  align-items: center;
}

.qa-review-button {
  justify-content: flex-start;
  text-align: left;
  padding-inline: 0;
}

.confirmation-modal-message {
  max-width: 30rem;
  margin: 8px auto 0;
  white-space: normal;
  text-align: left;
  word-break: break-word;
  display: grid;
  gap: var(--spacing-xs);
}

.dataland-tag {
  height: 1.75rem;
  padding: 0 0.625rem;
  font-size: 0.875rem;
  font-weight: 400;
  white-space: nowrap;
  vertical-align: middle;
  border-radius: 4px;
}
</style>
