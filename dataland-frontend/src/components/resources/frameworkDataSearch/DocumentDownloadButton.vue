<template>
  <PrimeButton
    class="uppercase p-button p-button-sm d-letters ml-3"
    aria-label="download document"
    @click="downloadDocumentFromButton"
    data-test="document-download-button"
  >
    <DownloadProgressSpinner :percent-completed="percentCompleted" v-if="downloadIsInProgress" />
    <span class="px-2 py-1" v-else>download</span>
  </PrimeButton>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import type Keycloak from 'keycloak-js';

import {
  createNewPercentCompletedRef,
  downloadDocument,
  type DocumentDownloadInfo,
} from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';
import DownloadProgressSpinner from '@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue';
import PrimeButton from 'primevue/button';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const percentCompleted = createNewPercentCompletedRef();

const downloadIsInProgress = computed(() => {
  return percentCompleted.value != undefined;
});

const props = defineProps({
  documentDownloadInfo: {
    type: Object as () => DocumentDownloadInfo,
    required: true,
  },
});

const downloadDocumentFromButton = async (): Promise<void> => {
  if (downloadIsInProgress.value) return;
  await downloadDocument(props.documentDownloadInfo, getKeycloakPromise, percentCompleted);
};
</script>
