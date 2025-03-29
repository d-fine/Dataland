<template>
  <div class="flex flex-column align-items-center justify-content-center">
    <h4 class="mt-4 text-center">
      Select any number of available reporting periods and click the Submit button. This will request access to all
      datasets for the selected reporting periods.
    </h4>
    <DataTable v-model:selection="selectedOptions" :value="requestableOptions" dataKey="reportingPeriod">
      <Column selectionMode="multiple" headerStyle="width: 3rem" />
      <Column field="reportingPeriod" header="Reporting Period" />
      <Column field="latestUpload" header="Latest Upload" />
    </DataTable>
    <div class="flex flex-column align-items-center justify-content-center col-6 mt-2">
      <PrimeButton
        :disabled="selectedOptions.length === 0"
        @click="submitDataRequestsForSelection"
        :class="selectedOptions.length > 0 ? 'mt-2' : 'button-disabled mt-2'"
        data-test="requestAccessButton"
      >
        <span class="d-letters pl-2"> Request Access </span>
      </PrimeButton>
      <FailMessage
        v-if="isAccessRequestFailed"
        message="Request failed. Please try again later or contact the Dataland team."
        :messageId="messageCounter"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

import FailMessage from '@/components/messages/FailMessage.vue';
import { computed, inject, ref } from 'vue';
import { type DataMetaInformation } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import PrimeButton from 'primevue/button';
import { useRouter } from 'vue-router';
import { type SingleDataRequestDataTypeEnum } from '@clients/communitymanager';
type RequestableOption = {
  reportingPeriod: string;
  latestUpload: string;
};

const props = defineProps<{
  metaInfoOfAvailableDatasets: DataMetaInformation[];
  dataType: string;
  companyId: string;
}>();

const emit = defineEmits(['submittedAccessRequests']);

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const selectedOptions = ref<RequestableOption[]>([]);
const messageCounter = ref(0);
const isAccessRequestFailed = ref(false);

const requestableOptions = computed<RequestableOption[]>(() => {
  const reportingPeriodToLatestUploadTime = new Map<string, number>();

  props.metaInfoOfAvailableDatasets.forEach((singleMetaInfo) => {
    const existingMaxUploadTime = reportingPeriodToLatestUploadTime.get(singleMetaInfo.reportingPeriod);
    if (existingMaxUploadTime === undefined || singleMetaInfo.uploadTime > existingMaxUploadTime) {
      reportingPeriodToLatestUploadTime.set(singleMetaInfo.reportingPeriod, singleMetaInfo.uploadTime);
    }
  });

  return Array.from(reportingPeriodToLatestUploadTime, ([reportingPeriod, maxUploadTime]) => ({
    reportingPeriod,
    latestUpload: new Date(maxUploadTime).toLocaleDateString('en-US'),
  }));
});

const router = useRouter();

/**
 * Submits data access requests for the current selection.
 */
async function submitDataRequestsForSelection(): Promise<void> {
  if (getKeycloakPromise) {
    try {
      messageCounter.value++;
      isAccessRequestFailed.value = false;
      const requestController = new ApiClientProvider(getKeycloakPromise()).apiClients.requestController;
      const reportingPeriodsToRequest = selectedOptions.value.map((it) => it.reportingPeriod);
      await requestController.postSingleDataRequest({
        companyIdentifier: props.companyId,
        dataType: props.dataType as SingleDataRequestDataTypeEnum,
        emailOnUpdate: false,
        // as unknown as Set<string> cast required to ensure proper json is created
        reportingPeriods: reportingPeriodsToRequest as unknown as Set<string>,
      });
      await router.push('/requests');
      emit('submittedAccessRequests');
    } catch {
      isAccessRequestFailed.value = true;
    }
  }
}
</script>
