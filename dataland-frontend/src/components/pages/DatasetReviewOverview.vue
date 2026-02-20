<template>
  <TheContent>
    <div class="card p-4 mb-4 border-round-xl surface-border border-1 surface-card">
      <div class="flex justify-content-between align-items-start">
        <div>
          <h1 class="text-3xl font-bold m-0 mb-2 text-left">
            <Skeleton v-if="isLoadingHeader" width="12rem" height="2rem" />
            <span v-else>{{ companyName }}</span>
          </h1>

          <div class="flex gap-3 text-color-secondary">
            <template v-if="isLoadingHeader">
              <Skeleton width="8rem" height="1rem" />
              <span>|</span>
              <Skeleton width="8rem" height="1rem" />
              <span>|</span>
              <Skeleton width="10rem" height="1rem" />
            </template>
            <template v-else>
              <span>Sector: {{ sector }}</span>
              <span>|</span>
              <span>Headquarter: {{ headquarter }}</span>
              <span>|</span>
              <span>LEI: {{ lei }}</span>
            </template>
          </div>
        </div>

        <div class="flex gap-2 align-items-center">
          <PrimeButton label="ADD TO PORTFOLIO" icon="pi pi-plus" @click="addToPortfolio" />
          <PrimeButton label="REQUEST DATA" icon="pi pi-file" @click="requestData" />
        </div>
      </div>
    </div>

    <div class="card p-4 mb-4 border-round-xl surface-border border-1 surface-card">
      <div v-if="isDatasetReviewPending || isCompanyDataPending" class="flex justify-content-center p-5">
        <i class="pi pi-spin pi-spinner" style="font-size: 2rem"></i>
      </div>
      <div v-else-if="isDatasetReviewError || isCompanyDataError">
        <p class="text-red-500">Failed to load dataset review or company information</p>
      </div>
      <div v-else>
        <div class="flex justify-content-between align-items-start mb-2">
          <div>
            <h2 class="text-2xl font-bold m-0 mb-3 text-left">
              <span>SFDR</span>
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
        <DatasetReviewComparisonTable />
      </div>
    </div>
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

// Props passed from the router
const props = defineProps<{
  dataId: string;
}>();

// Api Client
const apiClientProvider = useApiClient();

const {
  data: datasetReview,
  isPending: isDatasetReviewPending,
  isError: isDatasetReviewError,
} = useQuery({
  queryKey: ['qaReviewResponse', props.dataId],
  queryFn: async () => {
    const response = await apiClientProvider.apiClients.qaController.getQaReviewResponseByDataId(props.dataId);
    return response.data;
  },
  enabled: !!props.dataId,
});

const companyId = computed(() => datasetReview.value?.companyId);
const companyName = computed(() => datasetReview.value?.companyName ?? '—');

const { data: companyData, isPending: isCompanyDataPending } = useQuery({
  queryKey: ['metaDataForDataId', props.dataId],
  queryFn: async () => {
    const response = await apiClientProvider.backendClients.companyDataController.getCompanyById(companyId.value!);
    return response.data;
  },
  enabled: computed(() => !!companyId.value),
});

const isLoadingHeader = computed(() => isDatasetReviewPending.value || isCompanyDataPending.value);
const sector = computed(() => companyData.value?.companyInformation?.sector ?? '—');
const headquarter = computed(() => companyData.value?.companyInformation?.headquarters ?? '—');
const lei = computed(() => {
  const leiArray = companyData.value?.companyInformation?.identifiers?.Lei;
  return leiArray && leiArray.length > 0 ? leiArray[0] : '—';
});

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
