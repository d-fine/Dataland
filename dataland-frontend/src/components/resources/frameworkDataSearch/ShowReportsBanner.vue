<template>
  <div class="col-12 text-left bg-white mb-4 mp-0 all-sides-blue-border" style="width: 99%">
    <p class="font-bold text-gray-800 mt-0 mb-0 mp-0 ml-0 mr-0" style="font-size: 11pt">
      Data extracted from the company report. Company reports:
    </p>
    <p id="reportList">
      {{ resetReportsCount() }}
      <span v-for="[name, rep] in Object.entries(reports)" :key="name">
        {{ reportPlus() }}
        <button @click="downloadReport(rep.reference)">{{ name }}</button>
        <span v-if="reportCounter < Object.keys(reports).length">, </span>
      </span>
    </p>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import { CompanyReport } from "@clients/backend";
import { AxiosResponse, AxiosResponseHeaders } from "axios";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "ShowReportsBanner",
  props: {
    reports: { type: Map<string, CompanyReport> },
  },
  data() {
    return {
      reportCounter: 10,
      test: "test",
      getDocumentsFromStorageProcessed: false,
      getDocumentsFromStorageResponse: null as AxiosResponse<File> | null,
      reportContents: [File],
      messageCount: 0,
    };
  },
  computed: {},
  methods: {
    /**
     * Counter to correctly build the list of reports
     */
    reportPlus() {
      this.reportCounter++;
    },
    /**
     * Rsets the counter used to correctly build the list of reports
     */
    resetReportsCount() {
      this.reportCounter = 0;
    },
    /**
     * Method to download available reports
     *
     * @param reference hash of the report to be downloaded
     */
    async downloadReport(reference: string) {
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
            console.log(getDocumentsFromStorageResponse);
          });
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * construct file name from response header, as a fallback the hash is used as file name
     *
     * @param contentDisposition the part of the header that should contain the file name
     * @param reference the hash as a fallback value
     * @returns filename the name of the downloaded file
     */
    constructFileName(contentDisposition: string, reference: string): string {
      let filename: string;
      try {
        filename =
          contentDisposition
            .split(";")
            .find((n: string | string[]) => n.includes("filename="))
            .replace("filename=", "")
            .trim() ?? reference + ".pdf";
      } catch {
        filename = reference + ".pdf";
      }
      return filename;
    },
  },
});
</script>

<style>
a:link {
  color: var(--yellow-700);
}
</style>
