<template>
  <span
    @click="downloadReport()"
    class="font-semibold underline text-primary cursor-pointer"
    data-test="Report-Download"
    >{{ name }}</span
  >
  <span v-if="index < reportsNumber"> | </span>
</template>

<script lang="ts">
import { defineComponent, inject, PropType } from "vue";
import Keycloak from "keycloak-js";
import { AxiosResponse, AxiosResponseHeaders } from "axios";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { CompanyReport } from "@clients/backend";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "ReportLink",
  props: {
    name: { type: String, required: true },
    report: { type: Object as PropType<CompanyReport>, required: true },
    index: Number,
    reportsNumber: Number,
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
    async downloadReport() {
      const reference: string = this.report.reference;
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
            const contentDisposition: string =
              ((getDocumentsFromStorageResponse.headers as AxiosResponseHeaders).get(
                "content-disposition"
              ) as string) ?? "";
            const filename = this.constructFileName(contentDisposition, reference);

            docUrl.setAttribute("download", filename);
            document.body.appendChild(docUrl);
            docUrl.click();
          });
      } catch (error) {
        console.error(error);
      }
      console.log(reference);
    },

    /**
     * construct file name from response header, as a fallback the hash is used as file name
     *
     * @param contentDisposition the part of the header that should contain the file name
     * @param reference the hash as a fallback value
     * @returns filename the name of the downloaded file
     */
    constructFileName(contentDisposition: string, reference: string): string {
      const regex = /(?<=filename=)[^;]+/;
      const filename = regex.exec(contentDisposition);
      if (filename !== null) {
        return filename[0];
      } else {
        return reference + ".pdf";
      }
    },
  },
});
</script>
