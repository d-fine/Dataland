<template>
  <div class="text-primary">
    <a
      @click="downloadDocument()"
      class="cursor-pointer"
      :class="fontStyle"
      :title="downloadName"
      :data-test="'download-link-' + downloadName"
      style="display: grid; grid-template-columns: fit-content(100%) max-content 1.5em 0.5em 1.5em"
    >
      <span
        class="underline pl-1"
        style="overflow: hidden; text-overflow: ellipsis"
        :data-test="'Report-Download-' + downloadName"
        >{{ label ?? downloadName }}</span
      >
      <span v-if="suffix" class="underline ml-1 pl-1">{{ suffix }}</span>
      <i
        v-if="showIcon"
        class="pi pi-download pl-1"
        data-test="download-icon"
        aria-hidden="true"
        style="font-size: 12px; margin: auto"
      />
      <span> </span>
      <DownloadProgressSpinner :percent-completed="percentCompleted" />
    </a>
  </div>
</template>

<script setup lang="ts">
import { computed, inject, provide, ref } from 'vue';
import type Keycloak from 'keycloak-js';

import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type PrivateFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import {
  getAllPrivateFrameworkIdentifiers,
  getBasePrivateFrameworkDefinition,
} from '@/frameworks/BasePrivateFrameworkRegistry';
import DownloadProgressSpinner from '@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue';
import { getFileExtensionFromHeaders, getMimeTypeFromHeaders } from '@/utils/Axios';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const percentCompleted = ref<number | undefined>(undefined);

provide('percentCompleted', percentCompleted);

const props = defineProps({
  label: String,
  suffix: String,
  page: { type: Number, required: false },
  downloadName: { type: String, required: true },
  fileReference: { type: String, required: true },
  dataId: String,
  dataType: String,
  showIcon: Boolean,
  fontStyle: String,
});

const isForDownloadOfPrivateFrameworkDocument = computed(() => {
  return !!props.dataType && getAllPrivateFrameworkIdentifiers().includes(props.dataType);
});

/**
 * Method to download available reports
 */
const downloadDocument = async (): Promise<void> => {
  percentCompleted.value = 0;
  try {
    const docUrl = document.createElement('a');
    if (isForDownloadOfPrivateFrameworkDocument.value) {
      await handlePrivateDocumentDownload(props.fileReference, docUrl);
    } else {
      await handlePublicDocumentDownload(props.fileReference, docUrl);
    }
  } catch (error) {
    console.error(error);
  }
  percentCompleted.value = undefined;
};

/**
 * This method retrieves the documents for private data frameworks
 * @param fileReference hash of the document to be retrieved
 * @param docUrl initial reference of the document reference
 */
const handlePrivateDocumentDownload = async (fileReference: string, docUrl: HTMLAnchorElement): Promise<void> => {
  if (!props.dataId) throw new Error('Data id is required for private framework document download');
  if (!props.dataType) throw new Error('Data type is required for private framework document download');

  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
  let privateDataControllerApi: PrivateFrameworkDataApi<unknown>;
  const frameworkDefinition = getBasePrivateFrameworkDefinition(props.dataType);
  if (frameworkDefinition) {
    privateDataControllerApi = frameworkDefinition.getPrivateFrameworkApiClient(
      undefined,
      apiClientProvider.axiosInstance
    );
    let downloadCompleted = false;
    await privateDataControllerApi
      .getPrivateDocument(props.dataId, fileReference, {
        responseType: 'arraybuffer',
        onDownloadProgress: (progressEvent) => {
          if (!downloadCompleted && progressEvent.total != null)
            percentCompleted.value = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        },
      })
      .then((getDocumentsFromStorageResponse) => {
        downloadCompleted = true;
        percentCompleted.value = 100;
        const fileExtension = getFileExtensionFromHeaders(getDocumentsFromStorageResponse.headers);
        const mimeType = getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
        const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
        docUrl.href = URL.createObjectURL(newBlob);
        docUrl.setAttribute('download', `${props.downloadName}.${fileExtension}`);
        document.body.appendChild(docUrl);
        docUrl.click();
      });
  }
};

/**
 * This method retrieves the documents for public data frameworks
 * @param fileReference hash of the document to be retrieved
 * @param docUrl initial reference of the document reference
 */
const handlePublicDocumentDownload = async (fileReference: string, docUrl: HTMLAnchorElement): Promise<void> => {
  const documentControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
    .documentController;
  let downloadCompleted = false;
  await documentControllerApi
    .getDocument(fileReference, {
      responseType: 'arraybuffer',
      onDownloadProgress: (progressEvent) => {
        if (!downloadCompleted && progressEvent.total != null)
          percentCompleted.value = Math.round((progressEvent.loaded * 100) / progressEvent.total);
      },
    })
    .then((getDocumentsFromStorageResponse) => {
      downloadCompleted = true;
      percentCompleted.value = 100;
      const mimeType = getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
      const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
      docUrl.href = URL.createObjectURL(newBlob);
      docUrl.target = '_blank';
      docUrl.dataset.test = `report-${props.downloadName}-link`;
      if (props.page) docUrl.href += `#page=${props.page}`;
      document.body.appendChild(docUrl);
      docUrl.click();
    });
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
