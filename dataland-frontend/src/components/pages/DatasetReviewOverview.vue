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
          <div v-else>
            <div class="flex justify-content-between align-items-start mb-2">
              <div>
                <h2 class="text-2xl font-bold m-0 mb-3 text-left">
                  <span>{{ frameworkNameAsString }}</span>
                </h2>
                <div class="font-italic mb-1 text-left">
                  {{ dataPointsLeftToReview }} / {{ dataPointsTotal }} data points to review
                </div>

                <div class="flex align-items-center gap-4">
                  <span class="text-color-secondary">Data extracted from:</span>
                  <span class="text-primary font-medium cursor-pointer">Annual_Report_2024</span>
                  <span class="text-primary font-medium cursor-pointer underline">All documents</span>
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
                  />
                  <PrimeButton label="FINISH REVIEW" severity="success" icon="pi pi-check" @click="finishReview" />
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
              :data-id="props.dataId"
              :dataset-review="datasetReview"
              :data-meta-information="dataMetaInformation!"
              :search-query="''"
              :hide-empty-fields="hideEmptyFields"
              data-test="datasetReviewComparisonTable"
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
    />
  </TheContent>
</template>

<script setup lang="ts">
import DatasetReviewComparisonTable from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import { ref, onMounted, computed, inject } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import PrimeButton from 'primevue/button';
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import type { DatasetReviewOverview } from '@/utils/DatasetReviewOverview.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import ToggleSwitch from 'primevue/toggleswitch';
import CompanyInformationBanner from '@/components/pages/CompanyInformation.vue';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import PopupConfirmationModal from '@/components/resources/popups/PopupConfirmationModal.vue';
import { DatasetReviewState } from '@clients/qaservice';

// Props passed from the router
const props = defineProps<{
  dataId: string;
}>();

// Api Client
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = useApiClient();
const currentUserId = ref<string | undefined>(undefined);
const queryClient = useQueryClient();

// Empty Fields
const hideEmptyFields = ref(true);
// MOCK REVIEW OBJECT FOR NOW
const MOCK_DATASET_REVIEW: DatasetReviewOverview = {
  dataSetReviewId: 'rev-123',
  datasetId: props.dataId,
  companyId: '9af067dc-8280-4172-8974-1ae363c56260',
  dataType: 'sfdr',
  reportingPeriod: '2021',
  reviewState: 'Pending',
  qaJudgeUserId: '123',
  qaJudgeUserName: 'Jane Doe',
  qaReporterCompanies: [
    { reporterCompanyName: 'Company A', reporterCompanyId: 'COMP-A' },
    { reporterCompanyName: 'Company B', reporterCompanyId: 'COMP-B' },
  ],
  dataPoints: {
    extendedDateFiscalYearEnd: {
      dataPointTypeId: 'extendedDateFiscalYearEnd',
      dataPointId: 'dp-001',
      qaReports: [
        {
          qaReportId: 'QAR-A-1',
          verdict: 'QaAccepted',
          correctedData: null,
          reporterUserId: 'bot-a',
          reporterCompanyId: 'COMP-A',
        },
        {
          qaReportId: 'QAR-B-1',
          verdict: 'QaRejected',
          correctedData: '{"value":"2026-12-31","quality":"Incomplete"}',
          reporterUserId: 'bot-b',
          reporterCompanyId: 'COMP-B',
        },
      ],
      acceptedSource: 'Original',
      companyIdOfAcceptedQaReport: null,
      customValue: null,
    },
  },
};

const {
  data: datasetReview,
  isPending: isDatasetReviewPending,
  isError: isDatasetReviewError,
} = useQuery({
  queryKey: ['qaReviewResponse', props.dataId],
  queryFn: async () => {
    // TODO: REPLACE WITH REAL API CALL
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Return the mock instead of the API call
    return MOCK_DATASET_REVIEW;

    //const response = await apiClientProvider.apiClients.datasetReviewController.getDatasetReviewsByDatasetId(
    //  props.dataId
    //);
    //console.log('Dataset Review Response:', response.data);
    //return response.data[0] ?? null;
  },
  enabled: !!props.dataId,
});

const { data: dataMetaInformation, isPending: isDataMetaInformationPending } = useQuery({
  queryKey: ['frameworkData', props.dataId],
  queryFn: async () => {
    const response = await apiClientProvider.backendClients.metaDataController.getDataMetaInfo(props.dataId);
    return response.data;
  },
});

const companyId = computed(() => dataMetaInformation.value?.companyId);

const isInitialLoading = computed(() => isDatasetReviewPending.value || isDataMetaInformationPending.value);

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

const isAssignedToCurrentUser = computed(() => {
  if (!datasetReview.value) return false;
  return datasetReview.value.qaJudgeUserId === currentUserId.value;
});

/*
const { mutate: assignToMeMutation, isPending: isAssigningToMe } = useMutation({
  mutationFn: async () => {
    if (!datasetReview.value) throw new Error('No dataset review selected');
    return await apiClientProvider.apiClients.datasetReviewController.setReviewer(datasetReview.value.dataSetReviewId);
  },
  onSuccess: async () => {
    await queryClient.invalidateQueries({ queryKey: ['qaReviewResponse', props.dataId] });
    console.log('Successfully assigned!');
    confirmationModal.value.visible = false;
  },
  onError: (error) => {
    console.error('Error assigning dataset review:', error);
    confirmationModal.value.errorMessage = 'Failed to assign dataset review to yourself. Please try again.';
  },
});
*/

const { mutate: assignToMeMutation, isPending: isAssigningToMe } = useMutation({
  mutationFn: async () => {
    if (!datasetReview.value) throw new Error('No dataset review selected');

    await new Promise((resolve) => setTimeout(resolve, 1000));
    const fallbackId = currentUserId.value || 'local-dev-user-id';

    MOCK_DATASET_REVIEW.qaJudgeUserId = fallbackId;
    MOCK_DATASET_REVIEW.qaJudgeUserName = 'Current User (Mocked)';

    return { ...MOCK_DATASET_REVIEW };
  },
  onSuccess: (newDatasetReviewCopy) => {
    queryClient.setQueryData(['qaReviewResponse', props.dataId], newDatasetReviewCopy);

    if (!currentUserId.value) {
      currentUserId.value = 'local-dev-user-id';
    }
    confirmationModal.value.visible = false;

    console.log('Successfully assigned (Mocked)! UI should update now.');
    console.log('isAssignedToCurrentUser:', isAssignedToCurrentUser.value);
  },
  onError: (error) => {
    console.error('Error assigning dataset review:', error);
    confirmationModal.value.errorMessage = 'Failed to assign dataset review to yourself. Please try again.';
  },
});

const { mutate: rejectReviewMutation, isPending: isRejectReviewMutationPending } = useMutation({
  mutationFn: async () => {
    if (!datasetReview.value) throw new Error('No dataset review selected');
    return await apiClientProvider.apiClients.datasetReviewController.setReviewState(
      datasetReview.value.dataSetReviewId,
      DatasetReviewState.Aborted
    );
  },
  onSuccess: async () => {
    await queryClient.invalidateQueries({ queryKey: ['qaReviewResponse', props.dataId] });
    console.log('Rejected review!');
    confirmationModal.value.visible = false;
  },
  onError: (error) => {
    console.error('Error rejecting dataset review:', error);
    confirmationModal.value.errorMessage = 'Failed to reject dataset review. Please try again.';
  },
});

interface confirmationModelState {
  visible: boolean;
  header: string;
  message: string;
  errorMessage?: string;
  isLoading?: boolean;
  onConfirm?: () => void;
}
const confirmationModal = ref<confirmationModelState>({
  visible: false,
  header: '',
  message: '',
  errorMessage: '',
  isLoading: false,
  onConfirm: () => {},
});

const openConfirmationModal = (header: string, message: string, onConfirm?: () => void): void => {
  confirmationModal.value = {
    visible: true,
    header: header,
    message: message,
    errorMessage: '',
    onConfirm,
  };
};

const isModalActionPending = computed(() => isAssigningToMe.value || isRejectReviewMutationPending.value);

const assignToMe = (): void => {
  openConfirmationModal(
    'Assign Yourself',
    'Are you sure you want to assign this dataset review to yourself? This can only be undone by a dataland admin!',
    () => {
      assignToMeMutation();
    }
  );
};

const rejectDataset = (): void => {
  openConfirmationModal(
    'Reject Dataset',
    'Are you sure you want to reject this dataset review? This can only be undone by a dataland admin!',
    () => {
      rejectReviewMutation();
    }
  );
};

const finishReview = (): void => {
  alert(
    'Finish review logic here (seperate Ticket). Note: Make sure to -make sure all data is valid as is, then update the review state to finished '
  );
};

/**
 * Identifies and sets the current user's ID via the Keycloak token.
 * @returns {Promise<void>} Resolves when the user ID has been set.
 */
async function setCurrentUserId(): Promise<void> {
  const keycloak = await assertDefined(getKeycloakPromise)();
  currentUserId.value = keycloak.idTokenParsed?.sub;
}

onMounted(async () => {
  console.log('Loaded Review Page for Data ID:', props.dataId);
  await setCurrentUserId();
});
</script>

<style scoped>
/* Optional tweaks to match Figma exactly */
</style>
