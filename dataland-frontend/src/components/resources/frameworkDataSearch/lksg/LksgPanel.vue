<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading LkSG Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="kpiDataObjects.length && !waitingForData">
    <CompanyDataTable
      :kpiDataObjects="kpiDataObjects"
      :reportingPeriodsOfDataSets="listOfColumnIdentifierObjects"
      :kpiNameMappings="lksgKpisNameMappings"
      :kpiInfoMappings="lksgKpisInfoMappings"
      :subAreaNameMappings="lksgSubAreasNameMappings"
      tableDataTitle="LkSG Data"
    />
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { DataAndMetaInformationLksgData, DataMetaInformation, LksgData } from "@clients/backend";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import CompanyDataTable from "@/components/general/CompanyDataTable.vue";
import { lksgDataModel } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";

export default defineComponent({
  name: "LksgPanel",
  components: { CompanyDataTable },
  data() {
    return {
      firstRender: true,
      waitingForData: true,
      lksgDataAndMetaInfo: [] as Array<DataAndMetaInformationLksgData>,
      listOfColumnIdentifierObjects: [] as Array<{ dataId: string; reportingPeriod: string }>,
      kpiDataObjects: [] as { [index: string]: string | object; subAreaKey: string; kpiKey: string }[],
      lksgDataModel,
    };
  },
  props: {
    companyId: {
      type: String,
    },
    singleDataMetaInfoToDisplay: {
      type: Object as () => DataMetaInformation,
    },
  },
  watch: {
    companyId() {
      this.listOfColumnIdentifierObjects = [];
      void this.fetchData();
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.listOfColumnIdentifierObjects = [];
        void this.fetchData();
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    void this.fetchData();
    this.firstRender = false;
  },
  methods: {
    /**
     * Fetches all accepted LkSG datasets for the current company and converts them to the requried frontend format.
     */
    async fetchData() {
      try {
        this.waitingForData = true;
        const lksgDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getLksgDataControllerApi();
        if (this.singleDataMetaInfoToDisplay) {
          const singleLksgData = (
            await lksgDataControllerApi.getCompanyAssociatedLksgData(this.singleDataMetaInfoToDisplay.dataId)
          ).data.data as LksgData;

          this.lksgDataAndMetaInfo = [{ metaInfo: this.singleDataMetaInfoToDisplay, data: singleLksgData }];
        } else {
          this.lksgDataAndMetaInfo = (
            await lksgDataControllerApi.getAllCompanyLksgData(assertDefined(this.companyId))
          ).data;
        }
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
     * @param dataIdOfLksgDataset The value of the date kpi of an LkSG dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: object | string | number,
      subAreaKey: string,
      dataIdOfLksgDataset: string
    ): void {
      if (kpiKey === "totalRevenue" && typeof kpiValue === "number") {
        kpiValue = this.convertToMillions(kpiValue);
      }
      let indexOfExistingItem = -1;
      const kpiDataObject = {
        subAreaKey: subAreaKey == "general" ? `_${subAreaKey}` : subAreaKey,
        kpiKey: kpiKey,
        [dataIdOfLksgDataset]: kpiValue,
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
     * Retrieves and converts the stored array of LkSG datasets in order to make it displayable in the frontend.
     */
    convertLksgDataToFrontendFormat(): void {
      if (this.lksgDataAndMetaInfo.length) {
        this.lksgDataAndMetaInfo.forEach((oneLksgDataset: DataAndMetaInformationLksgData) => {
          const dataIdOfLksgDataset = oneLksgDataset.metaInfo?.dataId ?? "";
          const reportingPeriodOfLksgDataset = oneLksgDataset.metaInfo?.reportingPeriod ?? "";
          this.listOfColumnIdentifierObjects.push({
            dataId: dataIdOfLksgDataset,
            reportingPeriod: reportingPeriodOfLksgDataset,
          });
          for (const areaObject of Object.values(oneLksgDataset.data)) {
            for (const [subAreaKey, subAreaObject] of Object.entries(areaObject as object) as [string, object][]) {
              for (const [kpiKey, kpiValue] of Object.entries(subAreaObject) as [string, object][]) {
                this.createKpiDataObjects(kpiKey, kpiValue, subAreaKey, dataIdOfLksgDataset);
              }
            }
          }
        });
      }
      this.listOfColumnIdentifierObjects = sortReportingPeriodsToDisplayAsColumns(this.listOfColumnIdentifierObjects);
    },

    /**
     * Converts a number to millions with max two decimal places and adds "MM" at the end of the number.
     *
     * @param inputNumber The number to convert
     * @returns a string with the converted number and "MM" at the end
     */
    convertToMillions(inputNumber: number): string {
      return `${(inputNumber / 1000000).toLocaleString("en-GB", { maximumFractionDigits: 2 })} MM`;
    },
  },
});
</script>
