<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading LkSG Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="lksgData && !waitingForData">
    <CompanyDataTable
      :kpiDataObjects="kpiDataObjects"
      :dataDatesOfDataSets="listOfDatesToDisplayAsColumns"
      :kpiNameMappings="lksgKpiNameMappings"
      :kpiInfoMappings="lksgKpiInfoMappings"
      :subAreaNameMappings="lksgSubAreaNameMappings"
      tableDataTitle="LkSG Data"
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
  lksgSubAreaNameMappings,
  lksgKpiNameMappings,
  lksgKpiInfoMappings,
} from "@/components/resources/frameworkDataSearch/DataModelsTranslations";

export default defineComponent({
  name: "LksgPanel",
  components: { CompanyDataTable },
  data() {
    return {
      waitingForData: true,
      lksgData: [] as Array<LksgData>,
      listOfDatesToDisplayAsColumns: [] as Array<string>,
      kpiDataObjects: [] as { [index: string]: string | object; subAreaKey: string; kpiKey: string }[],
      lksgKpiNameMappings,
      lksgKpiInfoMappings,
      lksgSubAreaNameMappings,
    };
  },
  props: {
    companyId: {
      type: String,
    },
  },
  watch: {
    companyId() {
      this.listOfDatesToDisplayAsColumns = [];
      void this.fetchDataForAllDataIds();
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    void this.fetchDataForAllDataIds();
  },
  methods: {
    /**
     * Fetches all LkSG datasets for the current company and converts them to the requried frontend format.
     */
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

    /**
     * Creates kpi data objects to pass them to the data table.
     *
     * @param kpiKey The field name of a kpi
     * @param kpiValue The corresponding value to the kpiKey
     * @param subAreaKey The sub area to which the kpi belongs
     * @param dataDateOfLksgDataset The value of the date kpi of an LkSG dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: object | string | number,
      subAreaKey: string,
      dataDateOfLksgDataset: string
    ): void {
      if (kpiKey === "totalRevenue" && typeof kpiValue === "number") {
        kpiValue = this.convertToMillions(kpiValue);
      }
      let indexOfExistingItem = -1;
      const kpiDataObject = {
        subAreaKey: subAreaKey == "general" ? `_${subAreaKey}` : subAreaKey,
        kpiKey: kpiKey,
        [dataDateOfLksgDataset]: kpiValue,
      };
      indexOfExistingItem = this.kpiDataObjects.findIndex(
        (singleKpiDataObject) => singleKpiDataObject.kpiKey === kpiKey
      );
      if (indexOfExistingItem !== -1) {
        Object.assign(this.kpiDataObjects[indexOfExistingItem], kpiDataObject);
      } else {
        this.kpiDataObjects.push(kpiDataObject);
      }
    },

    /**
     * Sorts dates to ensure that LkSG datasets are displayed chronologically in the table of all LkSG datasets.
     */
    sortDatesToDisplayAsColumns(): void {
      this.listOfDatesToDisplayAsColumns.sort((dateA, dateB) => {
        if (Date.parse(dateA) < Date.parse(dateB)) {
          return 1;
        } else {
          return -1;
        }
      });
    },

    /**
     * Retrieves and converts the stored array of LkSG datasets in order to make it displayable in the frontend.
     */
    convertLksgDataToFrontendFormat(): void {
      this.lksgData.forEach((oneLksgDataset) => {
        const dataDateOfLksgDataset = oneLksgDataset.social?.general?.dataDate ?? "";
        this.listOfDatesToDisplayAsColumns.push(dataDateOfLksgDataset);
        for (const areaObject of Object.values(oneLksgDataset)) {
          for (const [subAreaKey, subAreaObject] of Object.entries(areaObject as object) as [string, object][]) {
            for (const [kpiKey, kpiValue] of Object.entries(subAreaObject) as [string, object][]) {
              this.createKpiDataObjects(kpiKey, kpiValue, subAreaKey, dataDateOfLksgDataset);
            }
          }
        }
      });
      this.sortDatesToDisplayAsColumns();
    },

    /**
     * Converts a number to millions with max two decimal places and adds "MM" at the end of the number.
     *
     * @param inputNumber The numbert to convert
     * @returns a string with the converted number and "MM" at the end
     */
    convertToMillions(inputNumber: number): string {
      return `${(inputNumber / 1000000).toLocaleString("en-GB", { maximumFractionDigits: 2 })} MM`;
    },
  },
});
</script>
