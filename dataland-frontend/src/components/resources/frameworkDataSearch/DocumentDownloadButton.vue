<template>
  <PrimeButton
    class="uppercase p-button p-button-sm d-letters ml-3"
    aria-label="DOWNLOAD DATA"
    @click="downloadDocumentFromButton"
    data-test="downloadDataButton"
  >
    <DownloadProgressSpinner
      :percent-completed="percentCompleted"
      v-if="downloadIsInProgress"
      @animationend="toggleDownloadIsInProgressFlag"
    />
    <span class="px-2 py-1" v-else>DOWNLOAD</span>
  </PrimeButton>
</template>

<script setup lang="ts">
import { inject, ref } from 'vue';
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

const downloadIsInProgress = ref(false);

const props = defineProps({
  documentDownloadInfo: {
    type: Object as () => DocumentDownloadInfo,
    required: true,
  },
});

const toggleDownloadIsInProgressFlag = (): void => {
  downloadIsInProgress.value = !downloadIsInProgress.value;
};

const downloadDocumentFromButton = async (): Promise<void> => {
  if (downloadIsInProgress.value) return;
  toggleDownloadIsInProgressFlag();
  await downloadDocument(props.documentDownloadInfo, getKeycloakPromise, percentCompleted);
};
</script>

<style scoped lang="scss"></style>
