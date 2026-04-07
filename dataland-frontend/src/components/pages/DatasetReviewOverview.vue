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
              :companyId="companyId ?? ''"
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
import { useDatasetReviewQuery } from '@/api-queries/qa-service/dataset-judgement/useDatasetReviewQuery.ts';
import { useDataMetaInfoQuery } from '@/api-queries/backend/meta-data/useDataMetaInfoQuery.ts';
import { useSetDatasetReviewStateMutation } from '@/api-queries/qa-service/dataset-judgement/useSetDatasetReviewStateMutation.ts';
import { useSetDatasetReviewJudge } from '@/api-queries/qa-service/dataset-judgement/useSetDatasetReviewJudge.ts';
import router from '@/router';
import { useConfirmationModal } from '@/components/resources/popups/useConfirmationModal.ts';
import type { DocumentOption } from '@/types/JudgeDialogTypes.ts';

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

const dataIdRef = computed(() => datasetReview.value?.datasetId);
const datasetJudgementIdRef = computed(() => props.datasetJudgementId);

const {
  data: datasetReview,
  isPending: isDatasetReviewPending,
  isError: isDatasetReviewError,
} = useDatasetReviewQuery({
  datasetJudgementId: datasetJudgementIdRef,
});

const { data: dataMetaInformation, isPending: isDataMetaInformationPending } = useDataMetaInfoQuery(dataIdRef);

const companyId = computed(() => dataMetaInformation.value?.companyId);
const isInitialLoading = computed(() => {
  const hasDataId = !!dataIdRef.value;
  return isDatasetReviewPending.value || (hasDataId && isDataMetaInformationPending.value);
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

/**
 * Callback function to receive the list of KPI rows from the ComparisonTable child component.
 * These are needed to populate the "Next data point" dropdown in the JudgeDialog.
 * @param rows
 */
function onKpiRowsBuilt(rows: CellRow[]): void {
  kpiRows.value = rows;
}

const { mutate: assignToMeMutation, isPending: isAssigningToMe } = useSetDatasetReviewJudge(datasetJudgementIdRef);

const { mutate: rejectReviewMutation, isPending: isRejectReviewMutationPending } = useSetDatasetReviewStateMutation(
  datasetJudgementIdRef,
  DatasetJudgementState.FinishedWithDatasetRejection
);

const { mutate: finishReviewMutation, isPending: isFinishReviewMutationPending } = useSetDatasetReviewStateMutation(
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
          confirmationModal.value.errorMessage = 'Failed to finish dataset review: ' + error.message;
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
