<template>
  <span
    @click="downloadDocument()"
    class="font-semibold underline text-primary cursor-pointer"
    :data-test="'Report-Download-' + name"
    >{{ name }}</span
  >
</template>

<script lang="ts">
import { defineComponent, inject, PropType } from "vue";
import Keycloak from "keycloak-js";
import { AxiosResponse, AxiosResponseHeaders } from "axios";
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
    name: { type: String, required: true },
    document: { type: Object as PropType<{ reference: string }>, required: true },
  },
  data() {
    return {
      reportCounter: 0,
      getDocumentsFromStorageProcessed: false,
      getDocumentsFromStorageResponse: null as AxiosResponse<File> | null,
      messageCount: 0,
    };
  },
  computed: {},
  methods: {
    /**
     * Method to download available reports
     */
    async downloadDocument() {
      const reference: string = this.document.reference;
      try {
        const docUrl = document.createElement("a");
        const documentControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getDocumentControllerApi();
        await documentControllerApi
          .getDocument(reference, {
            headers: { accept: "application/pdf" },
            responseType: "arraybuffer",
          })
          .then((getDocumentsFromStorageResponse) => {
            const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: "application/pdf" });
            docUrl.href = URL.createObjectURL(newBlob);
            docUrl.setAttribute("download", `${this.name}.pdf`);
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
