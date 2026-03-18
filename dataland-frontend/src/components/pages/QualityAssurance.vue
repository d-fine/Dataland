<template>
  <TheContent class="min-h-screen relative">
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_JUDGE">
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

        <PrimeButton
          variant="link"
          @click="resetFilterAndSearchBar"
          label="RESET FILTERS"
          data-test="reset-filters-button"
        />
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
            data-test="qa-review-section"
            @row-click="onRowClicked($event)"
            paginator
            paginator-position="top"
            :rows="10"
            :rowsPerPageOptions="[5, 10, 20, 50]"
            paginatorTemplate="RowsPerPageDropdown FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
            currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
            :pageLinkSize="5"
            sortMode="multiple"
            removableSort
            filterDisplay="menu"
            v-model:filters="filters"
          >
            <Column field="dataId" header="DATA ID">
              <template #body="slotProps">
                {{ slotProps.data.dataId }}
              </template>
            </Column>
            <Column field="companyName" header="COMPANY NAME">
              <template #body="slotProps">
                <span data-test="qa-review-company-name">{{ slotProps.data.companyName }}</span>
              </template>
            </Column>
            <Column
              field="framework"
              header="FRAMEWORK"
              :sortable="true"
              :filterMatchMode="FilterMatchMode.IN"
              :showFilterMenu="true"
              :showFilterOperator="false"
              :showFilterMatchModes="false"
              sortField="frameworkHumanized"
            >
              <template #body="slotProps">
                {{ slotProps.data.frameworkHumanized }}
              </template>
              <template #filter="{ filterModel, filterCallback }">
                <div class="flex align-items-center gap-2 px-2" style="min-width: 12rem">
                  <FrameworkDataSearchDropdownFilter
                    :modelValue="selectedFrameworks"
                    appendTo="body"
                    input-class="w-full"
                    :available-items="availableFrameworks"
                    filter-name="Framework"
                    data-test="framework-picker"
                    id="framework-filter"
                    filter-placeholder="Search by Frameworks"
                    :max-selected-labels="1"
                    selected-items-label="{0} frameworks selected"
                    @focusin.once="openFrameworkFilterDropdown"
                    @update:modelValue="
                      (items: Array<FrameworkSelectableItem> | null) => {
                        selectedFrameworks = items ?? [];
                        filterModel.value =
                          items && items.length
                            ? items.map((item: FrameworkSelectableItem) => item.frameworkDataType)
                            : null;
                        filterCallback();
                      }
                    "
                  />
                  <PrimeButton
                    type="button"
                    icon="pi pi-filter-slash"
                    size="small"
                    variant="text"
                    class="p-datatable-filter-clear-button"
                    @click="
                      () => {
                        selectedFrameworks = [];
                        filterModel.value = null;
                        filterCallback();
                      }
                    "
                  />
                </div>
              </template>
              <template #filterclear></template>
              <template #filterapply></template>
            </Column>
            <Column
              field="reportingPeriod"
              header="REPORTING PERIOD"
              :filterMatchMode="FilterMatchMode.IN"
              :showFilterMenu="true"
              :showFilterOperator="false"
              :showFilterMatchModes="false"
              :sortable="true"
            >
              <template #body="slotProps">
                {{ slotProps.data.reportingPeriod }}
              </template>
              <template #filter="{ filterModel, filterCallback }">
                <div class="flex align-items-center gap-2 px-2" style="min-width: 12rem">
                  <DatePicker
                    data-test="reporting-period-filter"
                    class="w-full"
                    input-class="w-full"
                    v-model="filterModel.value"
                    :updateModelType="'string'"
                    placeholder="Reporting Period"
                    :showIcon="true"
                    :manualInput="false"
                    view="year"
                    dateFormat="yy"
                    selectionMode="multiple"
                    @update:modelValue="filterCallback"
                  />
                  <PrimeButton
                    type="button"
                    icon="pi pi-filter-slash"
                    size="small"
                    variant="text"
                    class="p-datatable-filter-clear-button"
                    @click="
                      () => {
                        filterModel.value = null;
                        filterCallback();
                      }
                    "
                  />
                </div>
              </template>
              <template #filterclear></template>
              <template #filterapply></template>
            </Column>
            <Column header="SUBMISSION DATE" field="timestamp" :sortable="true">
              <template #body="slotProps">
                {{ convertUnixTimeInMsToDateString(slotProps.data.timestamp) }}
              </template>
            </Column>
            <Column
              field="priorityOfAssociatedDataSourcing"
              sortField="priorityWithNullHandling"
              header="PRIORITY"
              :sortable="true"
              filterMatchMode="between"
              :showFilterMenu="true"
              :showFilterOperator="false"
              :showFilterMatchModes="false"
            >
              <template #body="slotProps">
                <DatalandTag
                  v-if="
                    slotProps.data.priorityOfAssociatedDataSourcing !== undefined &&
                    slotProps.data.priorityOfAssociatedDataSourcing !== null
                  "
                  class="dataland-tag"
                  data-test="priority-tag"
                  :severity="dataSourcingPrioritySeverity(slotProps.data.priorityOfAssociatedDataSourcing!)"
                  :value="String(slotProps.data.priorityOfAssociatedDataSourcing!)"
                />
              </template>
              <template #filter="{ filterModel, filterCallback }">
                <div class="px-2" style="min-width: 12rem">
                  <span style="color: var(--text-color-secondary); font-size: 1rem; font-weight: 300">
                    Priorities:
                    {{ filterModel.value ? filterModel.value[0] + ' - ' + filterModel.value[1] : 'All' }}
                  </span>
                  <div class="flex align-items-center gap-3">
                    <Slider
                      data-test="priority-slider"
                      v-model="filterModel.value"
                      :min="1"
                      :max="10"
                      :step="1"
                      range
                      class="w-full"
                      @update:modelValue="filterCallback"
                    />
                    <PrimeButton
                      type="button"
                      icon="pi pi-filter-slash"
                      size="small"
                      variant="text"
                      class="p-datatable-filter-clear-button"
                      @click="
                        () => {
                          filterModel.value = null;
                          filterCallback();
                        }
                      "
                    />
                  </div>
                </div>
              </template>
              <template #filterclear> </template>
              <template #filterapply> </template>
            </Column>
            <Column field="numberQaReports" header="NUMBER OF QA REPORTS" :sortable="true">
              <template #body="slotProps">
                {{ slotProps.data.numberQaReports }}
              </template>
            </Column>
            <Column field="reviewStatus" header="REVIEW" class="qa-review-status-cell" :sortable="true">
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
      </div>
    </AuthorizationWrapper>
  </TheContent>
  <PopupConfirmationModal
    v-model:visible="confirmationModal.visible"
    :header="confirmationModal.header"
    :message="confirmationModal.message"
    :error-message="confirmationModal.errorMessage"
    :is-loading="isCreatingReview"
    @confirm="confirmationModal.onConfirm"
  />
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
import { KEYCLOAK_ROLE_JUDGE } from '@/utils/KeycloakRoles';
import { retrieveAvailableFrameworks } from '@/utils/RequestsOverviewPageUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import type Keycloak from 'keycloak-js';
import DatePicker from 'primevue/datepicker';
import Column from 'primevue/column';
import DataTable, { type DataTableRowClickEvent } from 'primevue/datatable';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import PrimeButton from 'primevue/button';
import Message from 'primevue/message';
import { inject, onMounted, ref, watch } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { AxiosError } from 'axios';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter.ts';
import PopupConfirmationModal from '@/components/resources/popups/PopupConfirmationModal.vue';
import { useConfirmationModal } from '@/components/resources/popups/useConfirmationModal.ts';
import { GetInfoOnDatasetsDataTypesEnum, type QaReviewResponse } from '@clients/qaservice';
import { FilterMatchMode } from '@primevue/core/api';
import Slider from 'primevue/slider';
import { type DataTypeEnum } from '@clients/backend';

const filters = ref({
  framework: {
    value: null as DataTypeEnum[] | null,
    matchMode: FilterMatchMode.IN,
  },
  reportingPeriod: {
    value: null,
    matchMode: FilterMatchMode.IN,
  },
  priorityOfAssociatedDataSourcing: {
    value: null,
    matchMode: FilterMatchMode.BETWEEN,
  },
});

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

type QaReviewRow = QaReviewResponse & {
  reviewStatus: string;
  priorityWithNullHandling: number;
  frameworkHumanized: string;
};
const displayDataOfPage = ref<QaReviewRow[]>([]);
const waitingForData = ref(true);
const searchBarInput = ref('');
const selectedFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const availableFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const showNotEnoughCharactersWarning = ref(false);
const selectedDataId = ref<string>('');
const isCreatingReview = ref(false);
const errorMessage = ref<string>('');
const priorityByDimensions = ref<Record<string, number>>({});

const debounceInMs = 300;
let timerId = 0;
let notEnoughCharactersWarningTimeoutId = 0;

const { confirmationModal, openConfirmationModal } = useConfirmationModal();

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

    const companyNameFilter = searchBarInput.value === '' ? undefined : searchBarInput.value;
    const response = await apiClientProvider.apiClients.qaController.getInfoOnPendingDatasets(companyNameFilter);
    displayDataOfPage.value = await Promise.all(
      response.data.map(async (row) => ({
        ...row,
        reviewStatus: await getReviewStatus(row.qaJudgeUserId, row.qaJudgeUserName),
        priorityWithNullHandling:
          row.priorityOfAssociatedDataSourcing === null || row.priorityOfAssociatedDataSourcing === undefined
            ? Number.MAX_SAFE_INTEGER
            : row.priorityOfAssociatedDataSourcing,
        frameworkHumanized: humanizeStringOrNumber(row.framework),
      }))
    );
    waitingForData.value = false;
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
    openConfirmationModal(
      'Start Review',
      'Are you sure you want to start a review for this dataset? ' +
        'Once started, the review cannot be deleted and will be visible for other reviewers on Dataland.',
      () => {
        void confirmStartReview();
      }
    );
  } else {
    void goToQaViewPage(qaDataObject.companyId, qaDataObject.framework, qaDataObject.dataId);
  }
}

/**
 * Navigates to the dataset review page for the dataset with the given dataId, companyId and framework.
 */
function goToQaViewPage(companyId: string, framework: string, dataId: string): ReturnType<typeof router.push> {
  // In the future, this is supposed to navigate to: `/qa/review/${datasetReviewId}`.
  // However, until the dataset review overview page is fully implemented, we navigate to the dataset view page.
  const qaUri = `/companies/${companyId}/frameworks/${framework}/${dataId}`;
  return router.push(qaUri);
}

/**
 * Confirms the start of a dataset review in the confirmation modal.
 * Creates a dataset review for the dataset with the selected data id and navigates to the corresponding dataset review page.
 */
async function confirmStartReview(): Promise<void> {
  isCreatingReview.value = true;

  try {
    const response = await apiClientProvider.apiClients.datasetJudgementController.postDatasetJudgement(
      selectedDataId.value
    );

    confirmationModal.value.visible = false;
    await goToQaViewPage(response.data.companyId, response.data.dataType, selectedDataId.value);
  } catch (error) {
    if (error instanceof AxiosError) {
      confirmationModal.value.errorMessage = formatAxiosErrorMessage(error);
    } else {
      confirmationModal.value.errorMessage = 'Failed to create dataset review.';
    }
    console.error(confirmationModal.value.errorMessage);
  } finally {
    isCreatingReview.value = false;
  }
}

/**
 * Resets selected frameworks and searchBarInput
 */
function resetFilterAndSearchBar(): void {
  selectedFrameworks.value = [];
  searchBarInput.value = '';

  filters.value.framework.value = null;
  filters.value.reportingPeriod.value = null;
  filters.value.priorityOfAssociatedDataSourcing.value = null;
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
 * Opens the framework filter dropdown immediately when the framework filter
 * component is clicked.
 * @param event the filter component focus event
 */
function openFrameworkFilterDropdown(event: FocusEvent): void {
  const target = event.currentTarget as HTMLElement | null;
  if (!target) return;
  target.click();
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

watch(searchBarInput, () => {
  const isValid = validateSearchBarInput();
  if (isValid) {
    if (timerId) {
      clearTimeout(timerId);
    }
    timerId = setTimeout(() => getQaDataForCurrentPage(), debounceInMs);
  }
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
