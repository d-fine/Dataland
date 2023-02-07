<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading Sfdr Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="sfdrData && !waitingForData">
    <CompanyDataTable
      :kpiDataObjects="kpiDataObjects"
      :dataDatesOfDataSets="listOfDatesToDisplayAsColumns"
      :kpiNameMappings="kpisNameMappings"
      :kpiInfoMappings="kpisInfoMappings"
      :subAreaNameMappings="subAreasNameMappings"
      tableDataTitle="SFDR data"
    />
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { SfdrData } from "@clients/backend";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import CompanyDataTable from "@/components/general/CompanyDataTable.vue";
import {
  subAreasNameMappings,
  kpisNameMappings,
  kpisInfoMappings,
} from "@/components/resources/frameworkDataSearch/DataModelsTranslations";

export default defineComponent({
  name: "SfdrPanel",
  components: { CompanyDataTable },
  data() {
    return {
      waitingForData: true,
      sfdrData: [] as Array<SfdrData>,
      listOfDatesToDisplayAsColumns: [] as Array<string>,
      kpiDataObjects: [] as { [index: string]: string | object; subAreaKey: string; kpiKey: string }[],
      kpisNameMappings,
      kpisInfoMappings,
      subAreasNameMappings,
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
     * Fetches all Sfdr datasets for the current company and converts them to the requried frontend format.
     */
    async fetchDataForAllDataIds() {
      try {
        this.waitingForData = true;
        const sfdrDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getSfdrDataControllerApi();
        this.sfdrData = (await sfdrDataControllerApi.getAllCompanySfdrData(assertDefined(this.companyId))).data;
        this.convertSfdrDataToFrontendFormat(this.sfdrData);
        console.log("sfdrData", this.sfdrData);
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
     * @param dataDateOfSfdrDataset The value of the date kpi of an Sfdr dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: object | string,
      subAreaKey: string,
      dataDateOfSfdrDataset: string
    ): void {
      if (kpiKey === "totalRevenue" && typeof kpiValue === "string") {
        kpiValue = this.convertToMillions(parseFloat(kpiValue));
      }
      let indexOfExistingItem = -1;
      const kpiDataObject = {
        subAreaKey: subAreaKey == "general" ? `_${subAreaKey}` : subAreaKey,
        kpiKey: kpiKey,
        [dataDateOfSfdrDataset]: kpiValue,
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
     * Sorts dates to ensure that Sfdr datasets are displayed chronologically in the table of all Sfdr datasets.
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
     * Retrieves and converts values from an array of SDFG datasets in order to make it displayable in the frontend.
     *
     * @param sfdrData The Sfdr dataset that shall be converted
     */
    convertSfdrDataToFrontendFormat(sfdrData: Array<SfdrData>): void {
      sfdrData.forEach((oneSfdrDataset) => {
        const dataDateOfSfdrDataset = oneSfdrDataset.social?.general?.fiscalYearEnd ?? "";
        this.listOfDatesToDisplayAsColumns.push(dataDateOfSfdrDataset);
        for (const areaObject of Object.values(oneSfdrDataset)) {
          for (const [subAreaKey, subAreaObject] of Object.entries(areaObject as object) as [string, object][]) {
            for (const [kpiKey, kpiValue] of Object.entries(subAreaObject) as [string, object][]) {
              this.createKpiDataObjects(kpiKey, kpiValue, subAreaKey, dataDateOfSfdrDataset);
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
