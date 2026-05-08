<template>
  <TheContent>
    <div
      v-if="isInitialLoading"
      class="card py-8 px-0 mb-4 border-round-xl surface-card flex flex-column align-items-center justify-content-center"
      style="min-height: 400px"
    >
      <p class="font-medium text-xl mt-3">Loading Review Information...</p>
      <DatalandProgressSpinner />
    </div>

    <template v-else>
      <div class="col-12 text-left p-3">
        <div class="card py-4 px-0 mb-4 border-round-xl surface-card">
          <div class="flex justify-content-between align-items-start">
            <CompanyInformationBanner
              :companyId="companyIdRef ?? ''"
              :show-single-data-request-button="false"
              :show-add-to-portfolio-button="false"
              class="w-12"
              data-test="companyInformationBanner"
            />
          </div>
        </div>

        <div class="card py-4 px-0 mb-4 surface-card">
          <div v-if="isDatasetReviewError">
            <p class="text-red-500">Failed to load dataset review or company information</p>
          </div>
          <div v-else-if="!datasetReview">
            <p class="text-color-secondary">No dataset review found for this dataset.</p>
          </div>
          <div v-else>
            <div class="flex justify-content-between align-items-start mb-2">
              <div>
                <h2 class="text-2xl font-bold m-0 mb-3 text-left">
                  <span>{{ frameworkNameAsString }}</span>
                </h2>
                <div class="font-italic mb-1 text-left">
                  {{ dataPointsLeftToReview }} / {{ dataPointsTotal }} data points to review
                </div>

                <div class="flex align-items-center gap-2">
                  <ToggleSwitch inputId="hideEmptyDataToggleButton" v-model="hideEmptyFields" />
                  <label for="hideEmptyDataToggleButton" class="font-semibold text-sm cursor-pointer">
                    Hide empty fields
                  </label>
                </div>
              </div>
              <div>
                <div v-if="isAssignedToCurrentUser" class="flex gap-2 align-items-center">
                  <div class="text-right mr-2">
                    <p class="font-medium m-0">Assigned to you</p>
                  </div>
                  <PrimeButton
                    label="REJECT DATASET"
                    severity="danger"
                    icon="pi pi-times"
                    outlined
                    @click="rejectDataset"
                    data-test="qaReviewPageRejectButton"
                  />
                  <PrimeButton
                    label="FINISH REVIEW"
                    severity="success"
                    icon="pi pi-check"
                    :disabled="!canFinishReview || isFinishReviewMutationPending"
                    @click="finishReview"
                    data-test="qaReviewPageFinishButton"
                  />
                </div>
                <div v-else class="text-left">
                  <PrimeButton
                    label="ASSIGN YOURSELF"
                    icon="pi pi-user"
                    :loading="isAssigningToMe"
                    :disabled="isAssigningToMe"
                    @click="assignToMe"
                  />
                  <p class="text-sm m-0 text-left">Currently assigned to:</p>
                  <p class="text-sm m-0 text-left">{{ datasetReview?.qaJudgeUserName ?? '' }}</p>
                </div>
              </div>
            </div>
            <div v-if="reviewWarnings.length > 0" class="mb-3">
              <div
                v-for="warning in reviewWarnings"
                :key="warning.id"
                :data-test="`review-warning-${warning.id}`"
                class="p-3 mb-2 border-round border-2"
                :class="{
                  'bg-gray-100 border-gray-700': warning.type === 'info',
                  'bg-yellow-100 border-yellow-700': warning.type === 'warning',
                  'bg-red-100 border-red-700': warning.type === 'error',
                }"
              >
                {{ warning.message }}
                <div v-if="warning.links && warning.links.length > 0" class="mt-1">
                  <span class="font-semibold">Other datasets:</span>
                  <div v-for="link in warning.links" :key="link.url">
                    <a :href="link.url" target="_blank" rel="noopener noreferrer" class="underline">{{ link.label }}</a>
                  </div>
                </div>
              </div>
            </div>
            <DatasetReviewComparisonTable
              v-if="datasetReview"
              :framework="dataMetaInformation!.dataType"
              :data-id="datasetReview.datasetId"
              :dataset-review="datasetReview"
              :data-meta-information="dataMetaInformation!"
              :search-query="''"
              :hide-empty-fields="hideEmptyFields"
              :row-clickable="isAssignedToCurrentUser"
              data-test="datasetReviewComparisonTable"
              @row-click="onComparisonTableRowClicked"
              @kpi-rows-built="onKpiRowsBuilt"
              @documents-built="onDocumentsBuilt"
            />
            <JudgeDialog
              v-if="isJudgeDialogOpen && judgeDialogDataPointTypeId && isAssignedToCurrentUser"
              :dataset-review-id="props.datasetJudgementId"
              :data-point-type-id="judgeDialogDataPointTypeId ?? ''"
              :kpi-rows="kpiRows"
              :available-documents="availableDocuments"
              v-model:is-open="isJudgeDialogOpen"
            />
          </div>
        </div>
      </div>
    </template>
    <PopupConfirmationModal
      v-model:visible="confirmationModal.visible"
      :header="confirmationModal.header"
      :message="confirmationModal.message"
      :error-message="confirmationModal.errorMessage"
      :is-loading="isModalActionPending"
      @confirm="confirmationModal.onConfirm"
      :is-success="isActionSuccess"
    />
  </TheContent>
</template>

<script setup lang="ts">
import DatasetReviewComparisonTable from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import JudgeDialog from '@/components/resources/datasetReview/JudgeDialog.vue';
import type { CellRow } from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import { ref, onMounted, computed, inject } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import PrimeButton from 'primevue/button';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import ToggleSwitch from 'primevue/toggleswitch';
import CompanyInformationBanner from '@/components/pages/CompanyInformation.vue';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import PopupConfirmationModal from '@/components/resources/popups/PopupConfirmationModal.vue';
import { DatasetJudgementState } from '@clients/qaservice';
import { EU_TAXONOMY_FRAMEWORK_FAMILY } from '@/utils/Frameworks.ts';
import { useDatasetJudgementQuery } from '@/api-queries/qa-service/dataset-judgement/useDatasetJudgementQuery.ts';
import { useDataMetaInfoQuery } from '@/api-queries/backend/meta-data/useDataMetaInfoQuery.ts';
import { useSetDatasetJudgementStateMutation } from '@/api-queries/qa-service/dataset-judgement/useSetDatasetJudgementStateMutation.ts';
import { useSetJudgeForDatasetJudgement } from '@/api-queries/qa-service/dataset-judgement/useSetJudgeForDatasetJudgement.ts';
import router from '@/router';
import { useConfirmationModal } from '@/components/resources/popups/useConfirmationModal.ts';
import type { DocumentOption } from '@/types/JudgeDialogTypes.ts';
import { usePostEnhancedRequestsSearchCountQuery } from '@/api-queries/data-sourcing/enhanced-request/usePostEnhancedRequestsSearchCountQuery.ts';
import { useGetCompanyInformationByIdInfoQuery } from '@/api-queries/backend/company-data/useGetCompanyInformationByIdInfoQuery.ts';
import { RequestState, type RequestSearchFilterString } from '@clients/datasourcingservice';
import { usePostMetaDataFiltersQuery } from '@/api-queries/backend/meta-data/usePostMetaDataFiltersQuery.ts';
import { type DataMetaInformationSearchFilter, type DataTypeEnum, QaStatus } from '@clients/backend';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter.ts';

const props = defineProps<{
  datasetJudgementId: string;
}>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const currentUserId = ref<string | undefined>(undefined);
const hideEmptyFields = ref(true);
const isJudgeDialogOpen = ref(false);
const judgeDialogDataPointTypeId = ref<string | undefined>(undefined);
const availableDocuments = ref<DocumentOption[]>([]);

/**
 * Callback function to receive the list of available documents for a data point from the ComparisonTable child component.
 * @param documents
 */
function onDocumentsBuilt(documents: DocumentOption[]): void {
  availableDocuments.value = documents;
}

/**
 * Callback function to handle clicks on rows in the ComparisonTable child component. Opens the JudgeDialog for the clicked data point.
 * @param row
 */
function onComparisonTableRowClicked(row: CellRow): void {
  judgeDialogDataPointTypeId.value = row.dataPointTypeId;
  isJudgeDialogOpen.value = true;
}

const datasetJudgementIdRef = computed(() => props.datasetJudgementId);

const {
  data: datasetReview,
  isPending: isDatasetReviewPending,
  isError: isDatasetReviewError,
} = useDatasetJudgementQuery({
  datasetJudgementId: datasetJudgementIdRef,
});

const datasetIdRef = computed(() => datasetReview.value?.datasetId);
const dataTypeRef = computed(() => datasetReview.value?.dataType);
const reportingPeriodRef = computed(() => datasetReview.value?.reportingPeriod);

const { data: dataMetaInformation, isPending: isDataMetaInformationPending } = useDataMetaInfoQuery(datasetIdRef);

const companyIdRef = computed(() => dataMetaInformation.value?.companyId);

const isInitialLoading = computed(() => {
  const hasDatasetId = !!datasetIdRef.value;
  return isDatasetReviewPending.value || (hasDatasetId && isDataMetaInformationPending.value);
});
const frameworkNameAsString = computed(() =>
  dataMetaInformation.value ? humanizeStringOrNumber(dataMetaInformation.value.dataType) : '—'
);

const dataPointsTotal = computed(() => {
  const dataPoints = datasetReview.value?.dataPoints ?? {};
  return Object.keys(dataPoints).length;
});

const dataPointsLeftToReview = computed(() => {
  const dataPoints = datasetReview.value?.dataPoints ?? {};
  return Object.values(dataPoints).filter((dataPoint) => dataPoint.acceptedSource === null).length;
});

const canFinishReview = computed(() => dataPointsLeftToReview.value === 0);

const isAssignedToCurrentUser = computed(() => {
  if (!datasetReview.value) return false;
  return datasetReview.value.qaJudgeUserId === currentUserId.value;
});

const kpiRows = ref<CellRow[]>([]);

// QARG Precheck Section.

const groupedDataTypeRef = computed<string[] | undefined>(() => {
  if (!dataTypeRef.value) return undefined;
  const currentType = dataTypeRef.value;
  if (EU_TAXONOMY_FRAMEWORK_FAMILY.includes(currentType)) {
    return EU_TAXONOMY_FRAMEWORK_FAMILY;
  }
  return [dataTypeRef.value];
});

const requestSearchFilters = computed<RequestSearchFilterString>(() => ({
  companyId: companyIdRef.value,
  dataTypes: groupedDataTypeRef.value,
  reportingPeriods: reportingPeriodRef.value ? [reportingPeriodRef.value] : undefined,
  requestStates: [RequestState.Open, RequestState.Processing],
}));

const metaDataSearchFilters = computed<DataMetaInformationSearchFilter[]>(() =>
  (groupedDataTypeRef.value ?? []).map((dataType) => ({
    companyId: companyIdRef.value,
    dataType: dataType as DataTypeEnum,
    reportingPeriod: reportingPeriodRef.value,
    onlyActive: false,
  }))
);

const arePrecheckQueriesEnabled = computed(
  () => !!companyIdRef.value && !!dataTypeRef.value && !!reportingPeriodRef.value
);

const {
  data: filteredRequestCount,
  isPending: isRequestCountPending,
  isError: isRequestCountError,
} = usePostEnhancedRequestsSearchCountQuery(requestSearchFilters, { enabled: arePrecheckQueriesEnabled });

const {
  data: companyData,
  isPending: isCompanyDataPending,
  isError: isCompanyDataError,
} = useGetCompanyInformationByIdInfoQuery(companyIdRef);

const {
  data: filteredMetaData,
  isPending: isMetaDataPending,
  isError: isMetaDataError,
} = usePostMetaDataFiltersQuery(metaDataSearchFilters, { enabled: arePrecheckQueriesEnabled });

const isRequestCountReady = computed(() => !isRequestCountPending.value && !isRequestCountError.value);
const isCompanyDataReady = computed(() => !isCompanyDataPending.value && !isCompanyDataError.value);
const isMetaDataReady = computed(() => !isMetaDataPending.value && !isMetaDataError.value);

const hasValidRequestState = computed(() => {
  return (filteredRequestCount.value ?? 0) > 0;
});

const hasAssignedSector = computed(() => !!companyData.value?.sector);

const matchingDatasetsWithPeriodAndType = computed(() => {
  return filteredMetaData.value ?? [];
});

const acceptedMatchingDatasets = computed(() =>
  matchingDatasetsWithPeriodAndType.value.filter((entry) => entry.qaStatus === QaStatus.Accepted)
);

const hasAcceptedMatchingDataset = computed(() => acceptedMatchingDatasets.value.length > 0);

const pendingMatchingDatasets = computed(() =>
  matchingDatasetsWithPeriodAndType.value.filter((entry) => entry.qaStatus === QaStatus.Pending)
);

const hasMultiplePendingMatchingDatasets = computed(() => pendingMatchingDatasets.value.length > 1);

const isViewingNewestPendingDataset = computed(() => {
  const viewedObject = pendingMatchingDatasets.value.find((entry) => entry.dataId === datasetIdRef.value);
  return !!viewedObject && pendingMatchingDatasets.value.every((entry) => viewedObject.uploadTime >= entry.uploadTime);
});

//QARG Precheck Warnings Section

type ReviewWarning = {
  id: string;
  message: string;
  type: 'info' | 'warning' | 'error';
  links?: { label: string; url: string }[];
};

const reviewWarnings = computed((): ReviewWarning[] => {
  const warnings: ReviewWarning[] = [];

  if (isRequestCountReady.value && !hasValidRequestState.value) {
    warnings.push({
      id: 'invalid-request-state',
      message: 'There is no related data request with status Open or Processing.',
      type: 'error',
    });
  }

  if (isCompanyDataReady.value && !hasAssignedSector.value) {
    warnings.push({ id: 'missing-sector', message: 'The company has no assigned sector.', type: 'warning' });
  }

  if (isMetaDataReady.value) {
    if (hasAcceptedMatchingDataset.value) {
      warnings.push({
        id: 'accepted-duplicate',
        message: 'There is already an accepted dataset with the same reporting period and framework.',
        type: 'warning',
        links: acceptedMatchingDatasets.value
          .filter((entry) => !!entry.ref)
          .map((entry) => ({ label: entry.dataId, url: entry.ref! })),
      });
    }

    if (hasMultiplePendingMatchingDatasets.value) {
      if (isViewingNewestPendingDataset.value) {
        warnings.push({
          id: 'pending-duplicate',
          message: 'There are multiple pending datasets. You are reviewing the newest upload.',
          type: 'info',
          links: pendingMatchingDatasets.value
            .filter((entry) => !!entry.ref && entry.dataId !== datasetIdRef.value)
            .map((entry) => ({ label: entry.dataId, url: entry.ref! })),
        });
      } else {
        warnings.push({
          id: 'not-newest-pending',
          message: 'There are multiple pending datasets. You are not reviewing the newest upload.',
          type: 'error',
          links: pendingMatchingDatasets.value
            .filter((entry) => !!entry.ref && entry.dataId !== datasetIdRef.value)
            .map((entry) => ({ label: entry.dataId, url: entry.ref! })),
        });
      }
    }
  }

  return warnings;
});

/**
 * Callback function to receive the list of KPI rows from the ComparisonTable child component.
 * These are needed to populate the "Next data point" dropdown in the JudgeDialog.
 * @param rows
 */
function onKpiRowsBuilt(rows: CellRow[]): void {
  kpiRows.value = rows;
}

const { mutate: assignToMeMutation, isPending: isAssigningToMe } =
  useSetJudgeForDatasetJudgement(datasetJudgementIdRef);

const { mutate: rejectReviewMutation, isPending: isRejectReviewMutationPending } = useSetDatasetJudgementStateMutation(
  datasetJudgementIdRef,
  DatasetJudgementState.FinishedWithDatasetRejection
);

const { mutate: finishReviewMutation, isPending: isFinishReviewMutationPending } = useSetDatasetJudgementStateMutation(
  datasetJudgementIdRef,
  DatasetJudgementState.FinishedWithDatasetAcceptance
);

const isModalActionPending = computed(
  () => isAssigningToMe.value || isRejectReviewMutationPending.value || isFinishReviewMutationPending.value
);
const isActionSuccess = ref(false);

const { confirmationModal, openConfirmationModal } = useConfirmationModal();

const assignToMe = (): void => {
  openConfirmationModal(
    'Assign Yourself',
    `Are you sure you want to assign this dataset review to yourself?
              If there is already a user assigned, they will be unassigned.`,
    () => {
      assignToMeMutation(undefined, {
        onSuccess: () => {
          confirmationModal.value.visible = false;
          confirmationModal.value.errorMessage = '';
        },
        onError: (error) => {
          confirmationModal.value.errorMessage = 'Failed to assign dataset review to yourself: ' + error.message;
        },
      });
    }
  );
};

const rejectDataset = (): void => {
  openConfirmationModal(
    'Reject Dataset',
    'Are you sure you want to reject the dataset and all ' +
      'underlying data points? This action will finish the review and cannot be undone.',
    () => {
      rejectReviewMutation(undefined, {
        onSuccess: () => {
          isActionSuccess.value = true;
          confirmationModal.value.message = 'Dataset successfully rejected. Rerouting to QA page ...';
          setTimeout(() => {
            confirmationModal.value.visible = false;
            isActionSuccess.value = false;
            void goToQaPage();
          }, 3200);
        },
        onError: (error) => {
          confirmationModal.value.errorMessage = 'Failed to reject dataset: ' + error.message;
        },
      });
    }
  );
};

const finishReview = (): void => {
  openConfirmationModal(
    'Finish Review',
    'Are you sure you want to mark this dataset review as finished?',
    () => {
      finishReviewMutation(undefined, {
        onSuccess: () => {
          isActionSuccess.value = true;
          confirmationModal.value.message = 'Dataset review completed. Rerouting to QA page ...';
          setTimeout(() => {
            confirmationModal.value.visible = false;
            isActionSuccess.value = false;
            void goToQaPage();
          }, 3200);
        },
        onError: (error) => {
          confirmationModal.value.errorMessage = 'Failed to finish dataset review: ' + formatAxiosErrorMessage(error);
        },
      });
    } // Implement action here in seperate ticket
  );
};

/**
 * Navigates the user to the Quality Assurance overview page.
 */
function goToQaPage(): ReturnType<typeof router.push> {
  return router.push({ name: 'UI for quality assurance' });
}

/**
 * Identifies and sets the current user's ID via the Keycloak token.
 * @returns {Promise<void>} Resolves when the user ID has been set.
 */
async function setCurrentUserId(): Promise<void> {
  const keycloak = await assertDefined(getKeycloakPromise)();
  currentUserId.value = keycloak.idTokenParsed?.sub;
}

onMounted(async () => {
  console.log('Loaded Review Page for Dataset Review ID:', props.datasetJudgementId);
  await setCurrentUserId();
});
</script>
