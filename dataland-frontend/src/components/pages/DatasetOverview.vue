<template>
  <TheContent class="relative">
    <div>
      <PrimeButton
        v-if="hasUserUploaderRights"
        icon="pi pi-plus"
        label="NEW DATASET"
        data-test="newDatasetButton"
        @click="linkToNewDataset()"
        :pt="{ root: { style: 'display: flex; margin:var(--spacing-sm)' } }"
      />
    </div>
    <DatasetOverviewTable
      data-test="datasetOverviewTable"
      :dataset-table-infos="datasetTableInfos"
      :class="datasetTableInfos.length > 0 ? '' : 'hidden'"
    />
    <div v-if="isError && !userUploads" class="text-center" data-test="datasetsErrorText">
      <p class="font-medium text-xl">Your datasets could not be loaded.</p>
      <p class="font-medium text-xl">Please try again later or contact the support team if the issue persists.</p>
    </div>
    <div v-else-if="isPending" class="inline-loading text-center">
      <p class="font-medium text-xl">Loading datasets...</p>
      <DatalandProgressSpinner />
    </div>
    <div v-else-if="datasetTableInfos.length === 0">
      <h1 class="mb-0" data-test="noDatasetUploadedText">No datasets uploaded</h1>
    </div>
  </TheContent>
</template>

<script setup lang="ts">
import { useGetUserUploadsDataMetaInformationQuery } from '@/api-queries/backend/user-uploads/useGetUserUploadsDataMetaInformationQuery';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import TheContent from '@/components/generics/TheContent.vue';
import DatasetOverviewTable from '@/components/resources/datasetOverview/DatasetOverviewTable.vue';
import { toDatasetTableInfo } from '@/components/resources/datasetOverview/DatasetTableInfo';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { useCurrentUser } from '@/utils/useCurrentUser';
import PrimeButton from 'primevue/button';
import { computed } from 'vue';
import router from '@/router';

const { userId: currentUserId, roles: currentUserRoles } = useCurrentUser();
const hasUserUploaderRights = computed(() => currentUserRoles.value.includes(KEYCLOAK_ROLE_UPLOADER));

const { data: userUploads, isPending, isError } = useGetUserUploadsDataMetaInformationQuery({ userId: currentUserId });

const datasetTableInfos = computed(() => (userUploads.value ?? []).map(toDatasetTableInfo));

/**
 * Links the current instance or context to a new dataset, establishing a connection or association with it.
 */
function linkToNewDataset(): void {
  void router.push('/companies/choose');
}
</script>
