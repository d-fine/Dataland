<template>
  <h4 class="mt-4">
    Select any number of available reporting periods and click the Submit button. This will request access to all
    datasets for the selected reporting periods.
  </h4>
  <DataTable v-model:selection="selectedOptions" :value="requestableOptions" dataKey="reportingPeriod">
    <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
    <Column field="reportingPeriod" header="Reporting Period"></Column>
    <Column field="latestUpload" header="Latest Upload"></Column>
  </DataTable>
  <PrimeButton
    :disabled="selectedOptions.length === 0"
    @click="submitDataRequestsForSelection"
    :class="selectedOptions.length > 0 ? 'mt-2' : 'button-disabled mt-2'"
    data-test="requestAccessButton"
  >
    <span class="d-letters pl-2"> Request Access </span>
  </PrimeButton>
</template>

<script setup lang="ts">
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

import { computed, inject, ref } from 'vue';
import { type DataMetaInformation, type DataTypeEnum } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import PrimeButton from 'primevue/button';
import { useRouter } from 'vue-router';
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
    const requestController = new ApiClientProvider(getKeycloakPromise()).apiClients.requestController;
    const reportingPeriodsToRequest = selectedOptions.value.map((it) => it.reportingPeriod);
    await requestController.postSingleDataRequest({
      companyIdentifier: props.companyId,
      dataType: props.dataType as DataTypeEnum,
      reportingPeriods: reportingPeriodsToRequest as unknown as Set<string>,
    });
    await router.push('/requests');
    emit('submittedAccessRequests');
    // TODO catch error and show smth?
  }
}
</script>
