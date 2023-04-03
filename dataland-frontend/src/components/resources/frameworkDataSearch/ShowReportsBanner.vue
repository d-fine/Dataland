<template>
  <div class="col-12 text-left bg-white mb-4 mp-0 all-sides-blue-border" style="width: 99%">
    <p class="font-bold text-gray-800 mt-0 mb-0 mp-0 ml-0 mr-0" style="font-size: 11pt">
      Data extracted from the company report. Company reports:
    </p>
    <p id="reportList">
      {{ resetReportsCount() }}
      <span v-for="[name, rep] in Object.entries(reports)" :key="name">
        {{ reportPlus() }}
        <a :href="rep.reference">{{ name }}</a> <span v-if="reportCounter < Object.keys(reports).length">, </span>
      </span>
    </p>
  </div>
</template>

<script lang="ts">
import {defineComponent, inject} from "vue";
import {CompanyReport} from "@clients/backend";
import {AxiosResponse} from "axios";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "ShowReportsBanner",
  props: {
    reports: {type: Map<string, CompanyReport>},
  },
  data() {
    return {
      reportCounter: 10,
      test: "test",
      getDocumentsFromStorageProcessed: false,
      getDocumentsFromStorageResponse: null as AxiosResponse<File> | null,
      messageCount: 0,
    };
  },
  computed: {},
  methods: {
    reportPlus() {
      this.reportCounter++;
    },
    resetReportsCount() {
      this.reportCounter = 0;
    },

    /**
     * Retrieves document for from the storage
     */
    async getDocumentsFromStorage(documentId: String): Promise<void> {
      try {
        this.getDocumentsFromStorageProcessed = false;
        this.messageCount++;
        const documentControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
        ).getDocumentControllerApi();
        this.getDocumentsFromStorageResponse = await documentControllerApi.getDocument(documentId);
      } catch (error) {
        this.getDocumentsFromStorageResponse = null;
        console.error(error);
      } finally {
        this.getDocumentsFromStorageProcessed = true;
      }
    },
    getAllReports() {
      try{Object.values(this.reports).forEach((report) =>
        {
          this.getDocumentsFromStorage(report.reference)
        })}
        catch(error) {
          console.error(error);
        }
    }
  },
});
</script>

<style>
a:link {
  color: var(--yellow-700);
  text-decoration: none;
}
</style>
