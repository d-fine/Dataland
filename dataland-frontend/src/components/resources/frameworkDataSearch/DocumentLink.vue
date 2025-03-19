<template>
  <div class="text-primary">
    <a @click="downloadDocument()" class="cursor-pointer" :class="fontStyle" style="flex: 0 0 auto">
      <span class="underline pl-1" :data-test="'Report-Download-' + downloadName">{{ label ?? downloadName }}</span>
      <i
        v-if="showIcon"
        class="pi pi-download pl-1"
        data-test="download-icon"
        aria-hidden="true"
        style="font-size: 12px"
      />
      <span class="underline ml-1 pl-1">{{ suffix }}</span>
    </a>
    <DownloadProgressSpinner :percent-completed="percentCompleted" />
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
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

export default defineComponent({
  name: 'DocumentLink',
  components: { DownloadProgressSpinner },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    return {
      percentCompleted: undefined as number | undefined,
    };
  },
  props: {
    label: String,
    suffix: String,
    page: { type: Number, required: false },
    downloadName: { type: String, required: true },
    fileReference: { type: String, required: true },
    dataId: String,
    dataType: String,
    showIcon: Boolean,
    fontStyle: String,
  },
  methods: {
    /**
     * Method to download available reports
     */
    async downloadDocument() {
      const fileReference: string = this.fileReference;
      this.percentCompleted = 0;
      try {
        const docUrl = document.createElement('a');
        if (this.isPrivateFrameworkDocumentLink) {
          await this.handlePrivateDocumentDownload(fileReference, docUrl);
        } else {
          await this.handlePublicDocumentDownload(fileReference, docUrl);
        }
      } catch (error) {
        console.error(error);
      }
      this.percentCompleted = undefined;
    },
    /**
     * This method retrieves the documents for private data frameworks
     * @param fileReference hash of the document to be retrieved
     * @param docUrl initial reference of the document reference
     */
    async handlePrivateDocumentDownload(fileReference: string, docUrl: HTMLAnchorElement) {
      if (!this.dataId) throw new Error('Data id is required for private framework document download');
      if (!this.dataType) throw new Error('Data type is required for private framework document download');

      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      let privateDataControllerApi: PrivateFrameworkDataApi<unknown>;
      const frameworkDefinition = getBasePrivateFrameworkDefinition(this.dataType);
      if (frameworkDefinition) {
        privateDataControllerApi = frameworkDefinition.getPrivateFrameworkApiClient(
          undefined,
          apiClientProvider.axiosInstance
        );
        let downloadCompleted = false;
        await privateDataControllerApi
          .getPrivateDocument(this.dataId, fileReference, {
            responseType: 'arraybuffer',
            onDownloadProgress: (progressEvent) => {
              if (!downloadCompleted && progressEvent.total != null)
                this.percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            },
          })
          .then((getDocumentsFromStorageResponse) => {
            downloadCompleted = true;
            this.percentCompleted = 100;
            const fileExtension = getFileExtensionFromHeaders(getDocumentsFromStorageResponse.headers);
            const mimeType = getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
            const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
            docUrl.href = URL.createObjectURL(newBlob);
            docUrl.setAttribute('download', `${this.downloadName}.${fileExtension}`);
            document.body.appendChild(docUrl);
            docUrl.click();
          });
      }
    },
    /**
     * This method retrieves the documents for public data frameworks
     * @param fileReference hash of the document to be retrieved
     * @param docUrl initial reference of the document reference
     */
    async handlePublicDocumentDownload(fileReference: string, docUrl: HTMLAnchorElement) {
      const documentControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
        .documentController;
      let downloadCompleted = false;
      await documentControllerApi
        .getDocument(fileReference, {
          responseType: 'arraybuffer',
          onDownloadProgress: (progressEvent) => {
            if (!downloadCompleted && progressEvent.total != null)
              this.percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          },
        })
        .then((getDocumentsFromStorageResponse) => {
          downloadCompleted = true;
          this.percentCompleted = 100;
          const mimeType = getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
          const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
          docUrl.href = URL.createObjectURL(newBlob);
          docUrl.target = '_blank';
          if (this.page) docUrl.href += `#page=${this.page}`;
          console.log(URL.createObjectURL(newBlob));
          document.body.appendChild(docUrl);
          docUrl.click();
        });
    },
  },
  computed: {
    isPrivateFrameworkDocumentLink(): boolean {
      return !!this.dataType && getAllPrivateFrameworkIdentifiers().includes(this.dataType);
    },
  },
});
</script>

<style scoped>
div {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: calc(41vw - 175px);
  @media only screen and (max-width: 768px) {
    max-width: calc(100vw - 200px);
  }
}
</style>
