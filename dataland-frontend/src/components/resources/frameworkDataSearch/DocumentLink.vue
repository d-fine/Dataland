<template>
  <div
    style="position: relative; display: flex; align-items: center; justify-content: flex-start"
    data-test="download-link"
  >
    <span @click="downloadDocument()" class="text-primary cursor-pointer" :class="fontStyle" style="flex: 0 0 auto">
      <span class="underline pl-1" :data-test="'Report-Download-' + downloadName">{{ label ?? downloadName }}</span>
      <i
        v-if="showIcon"
        class="pi pi-download pl-1"
        data-test="download-icon"
        aria-hidden="true"
        style="font-size: 12px"
      />
      <span class="underline ml-1 pl-1">{{ suffix }}</span>
    </span>
    <DownloadProgressSpinner :percent-completed="percentCompleted" />
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { type RawAxiosResponseHeaders } from 'axios';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type PrivateFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import {
  getAllPrivateFrameworkIdentifiers,
  getBasePrivateFrameworkDefinition,
} from '@/frameworks/BasePrivateFrameworkRegistry';
import DownloadProgressSpinner from '@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue';
import { getHeaderIfItIsASingleString } from '@/utils/Axios';

export default defineComponent({
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
  name: 'DocumentLink',
  components: { DownloadProgressSpinner },
  props: {
    label: String,
    suffix: String,
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
            const fileExtension = this.getFileExtensionFromHeaders(getDocumentsFromStorageResponse.headers);
            const mimeType = this.getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
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
          const fileExtension = this.getFileExtensionFromHeaders(getDocumentsFromStorageResponse.headers);
          const mimeType = this.getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
          const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
          docUrl.href = URL.createObjectURL(newBlob);
          docUrl.setAttribute('download', `${this.downloadName}.${fileExtension}`);
          document.body.appendChild(docUrl);
          docUrl.click();
        });
    },
    /**
     * Extracts the file extension from the http response headers
     * @param headers the headers of the get document http response
     * @returns the file type extension of the downloaded file
     */
    getFileExtensionFromHeaders(headers: RawAxiosResponseHeaders): DownloadableFileExtension {
      const contentDisposition = assertDefined(getHeaderIfItIsASingleString(headers, 'content-disposition')).split('.');
      return contentDisposition[contentDisposition.length - 1] as DownloadableFileExtension;
    },
    /**
     * Extracts the content type from the http response headers
     * @param headers the headers of the get document http response
     * @returns the mime type of the received document
     */
    getMimeTypeFromHeaders(headers: RawAxiosResponseHeaders): string {
      return assertDefined(getHeaderIfItIsASingleString(headers, 'content-type'));
    },
  },
  computed: {
    isPrivateFrameworkDocumentLink(): boolean {
      return !!this.dataType && getAllPrivateFrameworkIdentifiers().includes(this.dataType);
    },
  },
});
type DownloadableFileExtension = 'pdf' | 'xlsx' | 'xls' | 'ods';
</script>
