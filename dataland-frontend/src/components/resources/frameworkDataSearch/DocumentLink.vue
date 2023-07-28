<template>
  <span
    @click="downloadDocument()"
    class="text-primary cursor-pointer"
    :class="fontStyle"
    :data-test="'Report-Download-' + downloadName"
  >
    <span class="underline">
      {{ label ?? downloadName }}
    </span>
    <i
      v-if="showIcon"
      class="pi pi-download pl-1"
      data-test="download-icon"
      aria-hidden="true"
      style="font-size: 12px"
    />
  </span>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { AxiosRequestConfig } from "axios";
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
    downloadName: { type: String, required: true },
    reference: { type: String, required: true },
    showIcon: Boolean,
    fontStyle: String,
  },
  methods: {
    /**
     * Method to download available reports
     */
    async downloadDocument() {
      const reference: string = this.reference;
      try {
        const docUrl = document.createElement("a");
        const documentControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getDocumentControllerApi();
        await documentControllerApi
          .getDocument(reference, {
            headers: { accept: "application/pdf" },
            responseType: "arraybuffer",
          } as AxiosRequestConfig)
          .then((getDocumentsFromStorageResponse) => {
            const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: "application/pdf" });
            docUrl.href = URL.createObjectURL(newBlob);
            docUrl.setAttribute("download", `${this.downloadName}.pdf`);
            document.body.appendChild(docUrl);
            docUrl.click();
          });
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
