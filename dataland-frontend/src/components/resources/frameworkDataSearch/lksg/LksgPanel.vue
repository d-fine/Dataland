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
    lksgDataIds: {
      type: Array,
      default: () => [],
    },
  },
  watch: {
    lksgDataIds() {
      void this.fetchDataForAllDataIds(this.lksgDataIds as []);
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  methods: {
    async getCompanyLksgDataset(dataId: string) {
      try {
        const LksgDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getLksgDataControllerApi();
        const companyAssociatedData = await LksgDataControllerApi.getCompanyAssociatedLksgData(assertDefined(dataId));
        return companyAssociatedData.data.data;
      } catch (error) {
        console.error(error);
      }
    },

    async fetchDataForAllDataIds(dataIds: []) {
      this.waitingForData = true;
      this.lksgData = (await Promise.all(
        dataIds.map((dataId) => this.getCompanyLksgDataset(dataId))
      )) as Array<LksgData>; // TODO can Florians new endpoint make this to just one call?
      void this.convertLksgDataToFrontendFormat();
      this.waitingForData = false;
    },

    convertLksgDataToFrontendFormat(): void {
      this.listOfDatesToDisplayAsColumns = []
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
