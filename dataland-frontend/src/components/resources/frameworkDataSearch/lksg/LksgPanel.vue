<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading LkSG Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="lksgData && !waitingForData">
    <CompanyDataTable
      :dataSet="kpisDataObjects"
      :dataSetColumns="listOfDatesToDisplayAsColumns"
      :kpisNames="lksgKpis"
      :hintsForKpis="lksgQuestions"
      :impactTopicNames="impactTopicNames"
      tableDataTitle="LkSG data"
    />
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { LksgData } from "@clients/backend";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import CompanyDataTable from "@/components/general/CompanyDataTable.vue";
import {
  impactTopicNames,
  lksgKpis,
  lksgQuestions,
} from "@/components/resources/frameworkDataSearch/DataModelsTranslations";

export default defineComponent({
  name: "LksgPanel",
  components: { CompanyDataTable },
  data() {
    return {
      waitingForData: true,
      lksgData: [] as Array<LksgData> | undefined,
      listOfDatesToDisplayAsColumns: [] as string[],
      kpisDataObjects: [],
      lksgKpis,
      lksgQuestions,
      impactTopicNames,
    };
  },
  props: {
    companyId: {
      type: String,
      default: () => "",
    },
  },
  watch: {
    companyId() {
      void this.fetchDataForAllDataIds();
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    this.fetchDataForAllDataIds();
  },
  methods: {
    async fetchDataForAllDataIds() {
      try {
        this.waitingForData = true;
        const lksgDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getLksgDataControllerApi();
        this.lksgData = (await lksgDataControllerApi.getAllCompanyLksgData(assertDefined(this.companyId))).data;
        this.convertLksgDataToFrontendFormat();
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },

    convertLksgDataToFrontendFormat(): void {
      this.listOfDatesToDisplayAsColumns = [];
      this.lksgData?.forEach((oneLksgDataSet) => {
        let dataDate = "";
        for (const area of Object.values(oneLksgDataSet)) {
          for (const [topic, topicValues] of Object.entries(area)) {
            for (const [kpi, kpiValues] of Object.entries(topicValues as LksgData)) {
              let indexOfExistingItem = -1;
              if (kpi === "dataDate") {
                dataDate = kpiValues as string;
                this.listOfDatesToDisplayAsColumns.push(dataDate);
              }
              const singleKpiDataObject = {
                kpi: kpi,
                group: topic == "general" ? `_${topic}` : topic,
                [dataDate ? dataDate : ""]: kpiValues as string,
              };
              indexOfExistingItem = this.kpisDataObjects.findIndex((item) => item.kpi === kpi);

              if (indexOfExistingItem !== -1) {
                Object.assign(this.kpisDataObjects[indexOfExistingItem], singleKpiDataObject);
              } else {
                this.kpisDataObjects.push(singleKpiDataObject);
              }
            }
          }
        }
      });
    },
  },
});
</script>
