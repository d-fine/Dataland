<template>
  <span @click="downloadDocument()" class="text-primary cursor-pointer" :class="fontStyle">
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
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import type Keycloak from "keycloak-js";
import { type AxiosRequestConfig, type RawAxiosResponseHeaders } from "axios";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "DocumentLink",
  props: {
    label: String,
    suffix: String,
    downloadName: { type: String, required: true },
    fileReference: { type: String, required: true },
    showIcon: Boolean,
    fontStyle: String,
  },
  methods: {
    /**
     * Method to download available reports
     */
    async downloadDocument() {
      const fileReference: string = this.fileReference;
      try {
        const docUrl = document.createElement("a");
        const documentControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .documentController;
        await documentControllerApi
          .getDocument(fileReference, {
            responseType: "arraybuffer",
          } as AxiosRequestConfig)
          .then((getDocumentsFromStorageResponse) => {
            const fileExtension = this.getFileExtensionFromHeaders(getDocumentsFromStorageResponse.headers);
            const mimeType = this.getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
            const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
            docUrl.href = URL.createObjectURL(newBlob);
            docUrl.setAttribute("download", `${this.downloadName}.${fileExtension}`);
            document.body.appendChild(docUrl);
            docUrl.click();
          });
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Extracts the file extension from the http response headers
     * @param headers the headers of the get document http response
     * @returns the file type extension of the downloaded file
     */
    getFileExtensionFromHeaders(headers: RawAxiosResponseHeaders): DownloadableFileExtension {
      return assertDefined(new Map(Object.entries(headers)).get("content-disposition") as string)
        .split(".")
        .at(-1) as DownloadableFileExtension;
    },
    /**
     * Extracts the content type from the http response headers
     * @param headers the headers of the get document http response
     * @returns the mime type of the received document
     */
    getMimeTypeFromHeaders(headers: RawAxiosResponseHeaders): DownloadableFileExtension {
      return assertDefined(new Map(Object.entries(headers)).get("content-type") as string);
    },
  },
});
type DownloadableFileExtension = "pdf" | "xlsx" | "xls" | "ods";
</script>
