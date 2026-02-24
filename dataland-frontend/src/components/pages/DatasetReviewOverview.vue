<template>
  <TheContent>
    <div
      v-if="isInitialLoading"
      class="card p-8 mb-4 border-round-xl surface-card flex flex-column align-items-center justify-content-center"
      style="min-height: 400px"
    >
      <p class="font-medium text-xl mt-3">Loading Company Information...</p>
      <DatalandProgressSpinner />
    </div>

    <template v-else>
      <div class="card p-4 mb-4 border-round-xl surface-card">
        <div class="flex justify-content-between align-items-start">
          <div>
            <h1 class="text-3xl font-bold m-0 mb-2 text-left">
              {{ companyName }}
            </h1>

            <div class="flex gap-3 text-color-secondary">
              <span>Sector: {{ sector }}</span>
              <span>|</span>
              <span>Headquarter: {{ headquarter }}</span>
              <span>|</span>
              <span>LEI: {{ lei }}</span>
            </div>
          </div>

          <div class="flex gap-2 align-items-center">
            <PrimeButton label="ADD TO PORTFOLIO" icon="pi pi-plus" @click="addToPortfolio" />
            <PrimeButton label="REQUEST DATA" icon="pi pi-file" @click="requestData" />
          </div>
        </div>
      </div>

      <div class="card p-4 mb-4 surface-card">
        <div v-if="isDatasetReviewError || isCompanyDataError">
          <p class="text-red-500">Failed to load dataset review or company information</p>
        </div>
        <div v-else>
          <div class="flex justify-content-between align-items-start mb-2">
            <div>
              <h2 class="text-2xl font-bold m-0 mb-3 text-left">
                <span>{{ frameworkNameAsString }}</span>
              </h2>
              <div class="font-italic mb-1 text-left">98 / 107 datapoints to review</div>
              <div class="flex align-items-center gap-4">
                <span class="text-color-secondary">Data extracted from:</span>
                <span class="text-primary font-medium cursor-pointer">Annual_Report_2024</span>
                <span class="text-primary font-medium cursor-pointer underline">All documents</span>
              </div>
            </div>
            <div>
              <div class="flex gap-2 align-items-center">
                <div v-if="assignedToMe" class="text-right mr-2">
                  <p class="font-medium m-0">Assigned to you</p>
                </div>
                <PrimeButton v-else label="ASSIGN YOURSELF" icon="pi pi-user" @click="assignToMe" />
                <PrimeButton
                  label="REJECT DATASET"
                  severity="danger"
                  icon="pi pi-times"
                  outlined
                  @click="rejectDataset"
                />
                <PrimeButton label="FINISH REVIEW" severity="success" icon="pi pi-check" @click="finishReview" />
              </div>
              <div v-if="!assignedToMe" class="text-left">
                <p class="text-sm m-0 text-left">Currently assigned to:</p>
                <p class="text-sm m-0 text-left">{{ currentUserName }}</p>
              </div>
            </div>
          </div>
          <DatasetReviewComparisonTable
            v-if="datasetReview"
            :company-id="companyId ?? ''"
            :framework="dataMetaInformation!.dataType"
            :data-id="props.dataId"
            :dataset-review="datasetReview"
            :data-meta-information="dataMetaInformation!"
            :search-query="''"
          />
        </div>
      </div>
    </template>
  </TheContent>
</template>

<script setup lang="ts">
import DatasetReviewComparisonTable from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import { ref, onMounted, computed } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import PrimeButton from 'primevue/button';
import Skeleton from 'primevue/skeleton';
import { useQuery } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import type { DatasetReviewOverview } from '@/utils/DatasetReviewOverview.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';

// Props passed from the router
const props = defineProps<{
  dataId: string;
}>();

// Api Client
const apiClientProvider = useApiClient();

// MOCK REVIEW OBJECT FOR NOW
const MOCK_DATASET_REVIEW: DatasetReviewOverview = {
  datasetReviewId: 'rev-123',
  datasetId: props.dataId,
  companyId: 'comp-888',
  framework: 'sfdr',
  reportingPeriod: '2023',
  reviewState: 'Pending',
  dataPoints: {
    'datapoint-001': {
      dataPointTypeId: 'type-a',
      dataPointId: 'dp-001',
      qaReport: {
        qaReportId: 'qa-55',
        verdict: 'QaRejected',
        correctedData: '150.5',
      },
      acceptedSource: 'Qa',
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
    // Optional: Simulate network latency
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Return the mock instead of the API call
    return MOCK_DATASET_REVIEW;

    /* const response = await apiClientProvider.apiClients.qaController.getDatasetReviewOverview(props.dataId);
    return response.data;
    */
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

const {
  data: companyData,
  isPending: isCompanyDataPending,
  isError: isCompanyDataError,
} = useQuery({
  queryKey: ['companyData', companyId],
  queryFn: async () => {
    const response = await apiClientProvider.backendClients.companyDataController.getCompanyById(companyId.value!);
    return response.data;
  },
  enabled: computed(() => !!companyId.value),
});

const isInitialLoading = computed(
  () => isDatasetReviewPending.value || isCompanyDataPending.value || isDataMetaInformationPending.value
);
const isLoadingHeader = computed(() => isDatasetReviewPending.value || isCompanyDataPending.value);
const sector = computed(() => companyData.value?.companyInformation?.sector ?? '—');
const headquarter = computed(() => companyData.value?.companyInformation?.headquarters ?? '—');
const lei = computed(() => {
  const leiArray = companyData.value?.companyInformation?.identifiers?.Lei;
  return leiArray && leiArray.length > 0 ? leiArray[0] : '—';
});
const companyName = computed(() => companyData.value?.companyInformation?.companyName ?? '—');
const frameworkNameAsString = computed(() =>
  dataMetaInformation.value ? humanizeStringOrNumber(dataMetaInformation.value.dataType) : '—'
);

const currentUserName = ref('Max Mustermann');
const assignedToMe = ref(false);

// Actions
const assignToMe = (): void => {
  assignedToMe.value = true;
};
const rejectDataset = (): void => {
  alert('Reject logic here');
};
const finishReview = (): void => {
  alert('Finish review logic here');
};

const addToPortfolio = (): void => {
  alert('Add to portfolio logic here');
};

const requestData = (): void => {
  alert('Request data logic here');
};

onMounted(() => {
  console.log('Loaded Review Page for Data ID:', props.dataId);
});
</script>

<style scoped>
/* Optional tweaks to match Figma exactly */
</style>
