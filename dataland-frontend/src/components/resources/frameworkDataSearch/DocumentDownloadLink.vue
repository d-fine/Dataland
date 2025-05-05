<template>
  <div class="text-primary" style="max-width: 100%">
    <a
      @click="handleDocumentDownload()"
      class="cursor-pointer"
      :class="fontStyle"
      :title="documentDownloadInfo.downloadName"
      :data-test="'download-link-' + documentDownloadInfo.downloadName"
      style="display: grid; grid-template-columns: fit-content(100%) max-content max-content 0.5em 1.5em"
    >
      <span
        class="underline pl-1"
        style="overflow: hidden; text-overflow: ellipsis"
        :data-test="'Report-Download-' + documentDownloadInfo.downloadName"
        >{{ label ?? documentDownloadInfo.downloadName }}</span
      >
      <span class="underline ml-1 pl-1">{{ suffix ?? '' }}</span>
      <span>
        <i
          v-if="showIcon"
          class="pi pi-download pl-1"
          data-test="download-icon"
          aria-hidden="true"
          style="font-size: 12px; margin: auto"
        />
      </span>
      <span> </span>
      <DownloadProgressSpinner :percent-completed="percentCompleted" />
    </a>
  </div>
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

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const percentCompleted = createNewPercentCompletedRef();

const props = defineProps({
  label: String,
  suffix: String,
  documentDownloadInfo: {
    type: Object as () => DocumentDownloadInfo,
    required: true,
  },
  showIcon: Boolean,
  fontStyle: String,
});

const handleDocumentDownload = async (): Promise<void> => {
  if (downloadIsInProgress(percentCompleted.value)) return;
  await downloadDocument(props.documentDownloadInfo, getKeycloakPromise, percentCompleted);
};
</script>

<style scoped>
div {
  white-space: nowrap;
  max-width: calc(41vw - 175px);
  @media only screen and (max-width: 768px) {
    max-width: calc(100vw - 200px);
  }
}
</style>
