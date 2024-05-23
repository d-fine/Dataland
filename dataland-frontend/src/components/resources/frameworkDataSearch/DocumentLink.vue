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
import { Configuration, ConfigurationParameters, DataTypeEnum, type SmeData } from "@clients/backend";
import { getFrontendFrameworkDefinition } from "@/frameworks/FrontendFrameworkRegistry";
import { type PrivateFrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { DataPointDisplay } from "@/utils/DataPoint";
import { getBasePrivateFrameworkDefinition } from "@/frameworks/BasePrivateFrameworkRegistry";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  //TODO implementation of provide and inject dataId is bad, needs refactoring. For some reason direct injection
  //TODO of dataId here did not work. Goal is to remove the inject in DataPointWrapperDisplayComponent and inject
  //TODO dataId here
  inject: ["dialogRef"],
  data() {
    return {
      requestedDataId: String,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    this.requestedDataId = dialogRefToDisplay.data.dataId;
  },
  name: "DocumentLink",
  props: {
    label: String,
    suffix: String,
    downloadName: { type: String, required: true },
    fileReference: { type: String, required: true },
    showIcon: Boolean,
    fontStyle: String,
    datatype: String,
  },
  methods: {
    /**
     * Method to download available reports
     */
    async downloadDocument() {
      const fileReference: string = this.fileReference;
      //TODO use getAllPublicFrameworks and getAllPrivateFrameworks to define the if condition
      try {
        const docUrl = document.createElement("a");
        if (this.datatype == !DataTypeEnum.Sme) {
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
        } else {
          const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
          let SmeDataControllerApi: PrivateFrameworkDataApi<SmeData>;
          const frameworkDefinition = getBasePrivateFrameworkDefinition(DataTypeEnum.Sme);
          if (frameworkDefinition) {
            SmeDataControllerApi = frameworkDefinition.getPrivateFrameworkApiClient(
              undefined,
              apiClientProvider.axiosInstance,
            );
            await SmeDataControllerApi.getPrivateDocument(this.requestedDataId.toString(), fileReference, {
              responseType: "arraybuffer",
            } as AxiosRequestConfig).then((getDocumentsFromStorageResponse) => {
              const fileExtension = this.getFileExtensionFromHeaders(getDocumentsFromStorageResponse.headers);
              const mimeType = this.getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
              const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
              docUrl.href = URL.createObjectURL(newBlob);
              docUrl.setAttribute("download", `${this.downloadName}.${fileExtension}`);
              document.body.appendChild(docUrl);
              docUrl.click();
            });
          }
        }
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
    getMimeTypeFromHeaders(headers: RawAxiosResponseHeaders): string {
      return assertDefined(new Map(Object.entries(headers)).get("content-type") as string);
    },
  },
});
type DownloadableFileExtension = "pdf" | "xlsx" | "xls" | "ods";
</script>
