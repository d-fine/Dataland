<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading LkSG Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="lksgData && !waitingForData">
    <CompanyDataTable
      :dataSet="kpisDataObjects"
      :kpisNames="lksgKpis"
      :dataSetColumns="dataSetColumns"
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
      newDataSet: {},
      dataSetColumns: [] as string[],
      kpisDataObjects: [],
      lksgKpis,
      lksgQuestions,
      impactTopicNames,
    };
  },
  props: {
    companyID: {
      type: String,
      default: () => ""
    },
  },
  watch: {
    companyID() {
      void this.allDataSets();
    },
    lksgData() {
      void this.generateConvertedData();
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    this.allDataSets();
  },
  methods: {
    async allDataSets() {
      try {
        this.waitingForData = true;
        const lksgDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getLksgDataControllerApi();
        this.lksgData = (await lksgDataControllerApi.getAllCompanyLksgData(assertDefined(this.companyID!))).data;
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },

    generateConvertedData(): void {
      this.lksgData?.forEach((dataByYear) => {
        let dataDate = "";
        for (const area of Object.values(dataByYear)) {
          for (const [topic, topicValues] of Object.entries(area)) {
            for (const [kpi, kpiValues] of Object.entries(topicValues as LksgData)) { // TODO why as LksgData, dont we iterate over the fields of a whole LksgData structure
              let indexOfExistingItem = -1;
              if (kpi === "dataDate") { // TODO this only works as long as dataDate is the first entry in LksgData
                this.dataSetColumns.push(kpiValues as string);
                dataDate = kpiValues as string;
              }
              const singleKpiData = {
                kpi: kpi,
                group: topic == "general" ? `_${topic}` : topic,
                [dataDate ? dataDate : ""]: kpiValues as string,
              };
              indexOfExistingItem = this.kpisDataObjects.findIndex((item) => item.kpi === kpi);

              if (indexOfExistingItem !== -1) {
                Object.assign(this.kpisDataObjects[indexOfExistingItem], singleKpiData);
              } else {
                this.kpisDataObjects.push(singleKpiData);
              }
            }
          }
        }
      });
    },
  },
});
</script>
