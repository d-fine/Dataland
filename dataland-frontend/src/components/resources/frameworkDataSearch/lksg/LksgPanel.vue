<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading Lksg Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="dataSet && !waitingForData">
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
import { impactTopicNames, lksgKpis, lksgQuestions } from "@/components/resources/frameworkDataSearch/DataModelsTranslations";

export default defineComponent({
  name: "LksgPanel",
  components: { CompanyDataTable },
  data() {
    return {
      waitingForData: true,
      dataSet: [] as Array<LksgData> | undefined,
      newDataSet: {},
      dataSetColumns: [] as string[],
      kpisDataObjects: [],
      lksgKpis,
      lksgQuestions,
      impactTopicNames,
    };
  },
  props: {
    dataID: {
      type: Array,
      default: () => [],
    },
  },
  watch: {
    dataID() {
      void this.allDataSet(this.dataID as []);
    },
    dataSet() {
      void this.generateConvertedData();
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  methods: {
    async getCompanyLksgDataset(singleDataId: string) {
      try {
        this.waitingForData = true;
        const LksgDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getLksgDataControllerApi();
        const companyAssociatedData = await LksgDataControllerApi.getCompanyAssociatedLksgData(
          assertDefined(singleDataId)
        );
        this.waitingForData = false;
        return companyAssociatedData.data.data;
      } catch (error) {
        console.error(error);
      }
    },

    async allDataSet(ids: []) {
      this.dataSet = await Promise.all(ids.map((singleDataId) => this.getCompanyLksgDataset(singleDataId)));
    },

    generateConvertedData(): void {
      this.dataSet?.forEach((dataByYear) => {
        let dataDate = "";
        for (const area of Object.values(dataByYear)) {
          for (const [topic, topicValues] of Object.entries(area)) {
            for (const [kpi, kpiValues] of Object.entries(topicValues as LksgData)) {
              let indexOfExistingItem = -1;
              if (kpi === "dataDate") {
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
