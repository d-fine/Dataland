<template>
  <PrimeButton
    class="uppercase p-button p-button-sm d-letters ml-3"
    aria-label="download document"
    @click="handleDocumentDownload"
    data-test="document-download-button"
  >
    <DownloadProgressSpinner
      v-if="downloadIsInProgress(percentCompleted)"
      :percent-completed="percentCompleted"
      :white-spinner="true"
    />
    <span class="px-2 py-1" v-else>DOWNLOAD</span>
  </PrimeButton>
</template>

<script setup lang="ts">
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';

import {
  createNewPercentCompletedRef,
  downloadDocument,
  downloadIsInProgress,
  type DocumentDownloadInfo,
} from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';
import DownloadProgressSpinner from '@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue';
import PrimeButton from 'primevue/button';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const percentCompleted = createNewPercentCompletedRef();

const props = defineProps({
  documentDownloadInfo: {
    type: Object as () => DocumentDownloadInfo,
    required: true,
  },
});

const handleDocumentDownload = async (): Promise<void> => {
  if (downloadIsInProgress(percentCompleted.value)) return;
  await downloadDocument(props.documentDownloadInfo, getKeycloakPromise, percentCompleted);
};
</script>
<style scoped>
.d-letters {
  letter-spacing: 0.05em;
}
</style>
