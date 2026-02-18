<template>
  <TheContent>
    <div class="card p-4 mb-4 border-round-xl surface-border border-1 surface-card">
      <div class="flex justify-content-between align-items-start">
        <div>
          <h1 class="text-3xl font-bold m-0 mb-2">
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
          <div v-if="assignedToMe" class="text-right mr-2">
            <p class="font-medium m-0">Currently assigned to:</p>
            <p class="text-sm m-0">{{ currentUserName }}</p>
          </div>
          <PrimeButton v-else label="ASSIGN YOURSELF" icon="pi pi-user" @click="assignToMe" />

          <PrimeButton label="REJECT DATASET" severity="danger" icon="pi pi-times" outlined @click="rejectDataset" />
          <PrimeButton label="FINISH REVIEW" severity="success" icon="pi pi-check" @click="finishReview" />
        </div>
      </div>

      <div class="flex justify-content-between mt-4 pt-3 border-top-1 surface-border">
        <div class="flex align-items-center gap-2">
          <span class="text-color-secondary">Data extracted from:</span>
          <span class="text-primary font-medium cursor-pointer">Annual_Report_2024</span>
          <span class="text-primary font-medium cursor-pointer underline">All documents</span>
        </div>
        <div class="font-italic">98 / 107 datapoints to review</div>
      </div>
    </div>

    <div class="mb-4">
      <IconField>
        <InputIcon class="pi pi-search" />
        <InputText v-model="searchQuery" placeholder="Search KPI..." class="w-full" />
      </IconField>
    </div>

    <div v-if="isDatasetReviewPending || isCompanyDataPending" class="flex justify-content-center p-5">
      <i class="pi pi-spin pi-spinner" style="font-size: 2rem"></i>
    </div>
    <div v-else-if="isDatasetReviewError || isCompanyDataError">
      <p class="text-red-500">Failed to load dataset review or company information</p>
    </div>
    <div v-else>
      <DatasetReviewComparisonTable />
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import DatasetReviewComparisonTable from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import { ref, onMounted, computed } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import PrimeButton from 'primevue/button';
import InputText from 'primevue/inputtext';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
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
const searchQuery = ref('');

// Actions
const assignToMe = () => {
  assignedToMe.value = true;
};
const rejectDataset = () => {
  alert('Reject logic here');
};
const finishReview = () => {
  alert('Finish review logic here');
};

onMounted(() => {
  console.log('Loaded Review Page for Data ID:', props.dataId);
});
</script>

<style scoped>
/* Optional tweaks to match Figma exactly */
</style>
