<template>
  <div class="text-primary">
    <a
      v-if="isUserLoggedIn"
      @click="handleDocumentDownload()"
      class="cursor-pointer"
      :class="fontStyle"
      :title="documentDownloadInfo.downloadName"
      :data-test="'download-link-' + documentDownloadInfo.downloadName"
      style="display: grid; grid-template-columns: fit-content(100%) max-content max-content 1.5em"
    >
      <span
        class="underline pl-1"
        style="overflow: hidden; text-overflow: ellipsis"
        :data-test="'Report-Download-' + documentDownloadInfo.downloadName"
        >{{ label ?? documentDownloadInfo.downloadName }}</span
      >
      <span class="underline ml-1 pl-1">{{ suffix ?? '' }}</span>
      <span class="pr-2">
        <i
          v-if="showIcon"
          class="pi pi-download pl-1"
          data-test="download-icon"
          aria-hidden="true"
          style="font-size: 12px; margin: auto"
        />
      </span>
      <DownloadProgressSpinner :percent-completed="percentCompleted" />
    </a>
    <span
      v-else
      :class="fontStyle"
      :title="documentDownloadInfo.downloadName"
      :data-test="'download-text-' + documentDownloadInfo.downloadName"
      style="display: grid; grid-template-columns: fit-content(100%) max-content"
    >
      <span
        class="underline pl-1"
        style="overflow: hidden; text-overflow: ellipsis"
        :data-test="'Report-Download-' + documentDownloadInfo.downloadName"
        >{{ label ?? documentDownloadInfo.downloadName }}</span
      >
      <span class="underline ml-1 pl-1">{{ suffix ?? '' }}</span>
    </span>
  </div>
</template>

<script setup lang="ts">
import { inject, onMounted, ref } from 'vue';
import type Keycloak from 'keycloak-js';

import {
  createNewPercentCompletedRef,
  downloadDocument,
  downloadIsInProgress,
  type DocumentDownloadInfo,
} from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';
import DownloadProgressSpinner from '@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const percentCompleted = createNewPercentCompletedRef();

const isUserLoggedIn = ref<undefined | boolean>(undefined);

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

onMounted(() => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      isUserLoggedIn.value = keycloak.authenticated;
    })
    .catch((error) => console.error(error));
});

const handleDocumentDownload = async (): Promise<void> => {
  if (downloadIsInProgress(percentCompleted.value)) return;
  await downloadDocument(props.documentDownloadInfo, getKeycloakPromise, percentCompleted);
};
</script>

<style scoped>
div {
  white-space: nowrap;
  max-width: 100%;
}
</style>
