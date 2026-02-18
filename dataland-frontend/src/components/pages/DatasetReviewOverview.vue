<template>
  <TheContent>
    <div class="card p-4 mb-4 border-round-xl surface-border border-1 surface-card">
      <div class="flex justify-content-between align-items-start">

        <div>
          <h1 class="text-3xl font-bold m-0 mb-2">{{ companyName }}</h1>
          <div class="flex gap-3 text-color-secondary">
            <span>Sector: {{ sector }}</span>
            <span>|</span>
            <span>Headquarter: {{ headquarter }}</span>
            <span>|</span>
            <span>LEI: {{ lei }}</span>
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
        <div class="font-italic">
          98 / 107 datapoints to review
        </div>
      </div>
    </div>

    <div class="mb-4">
      <IconField>
        <InputIcon class="pi pi-search" />
        <InputText v-model="searchQuery" placeholder="Search KPI..." class="w-full" />
      </IconField>
    </div>

    <div class="card border-1 surface-border border-round-xl surface-card p-0 overflow-hidden">
      <div class="p-5 text-center surface-50">
        <i class="pi pi-table text-5xl text-400 mb-3"></i>
        <h3>Comparison Table Area</h3>
        <p>This is where the Original vs Corrected vs Custom columns will go.</p>
        <p class="text-sm text-color-secondary">Data ID: {{ dataId }}</p>
      </div>
    </div>

  </TheContent>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import TheContent from '@/components/generics/TheContent.vue';
import PrimeButton from 'primevue/button';
import InputText from 'primevue/inputtext';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';

// Props passed from the router
const props = defineProps<{
  dataId: string;
}>();

// Mock Data (Replace with API calls later)
const companyName = ref('adidas AG');
const sector = ref('Consumer Discretionary');
const headquarter = ref('Herzogenaurach');
const lei = ref('549300D3D0VPX4OKQE33');
const currentUserName = ref('Max Mustermann');
const assignedToMe = ref(false);
const searchQuery = ref('');

// Actions
const assignToMe = () => { assignedToMe.value = true; };
const rejectDataset = () => { alert('Reject logic here'); };
const finishReview = () => { alert('Finish review logic here'); };

onMounted(() => {
  console.log("Loaded Review Page for Data ID:", props.dataId);
});
</script>

<style scoped>
/* Optional tweaks to match Figma exactly */
</style>