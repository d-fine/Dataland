<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading LkSG Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="kpiDataObjects.length && !waitingForData">
    <LksgCompanyDataTable
      :kpiDataObjects="kpiDataObjects"
      :reportingPeriodsOfDataSets="listOfColumnIdentifierObjects"
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
import LksgCompanyDataTable from "@/components/resources/frameworkDataSearch/lksg/LksgCompanyDataTable.vue";
import { lksgDataModel } from "@/components/resources/frameworkDataSearch/lksg/LksgDataModel";
import { Subcategory } from "@/utils/GenericFrameworkTypes";
import { naceCodeMap } from "@/components/forms/parts/elements/derived/NaceCodeTree";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";
import { kpiDataObject } from "@/components/resources/frameworkDataSearch/KpiDataObject";

export default defineComponent({
  name: "LksgPanel",
  components: { LksgCompanyDataTable },
  data() {
    return {
      firstRender: true,
      waitingForData: true,
      lksgDataAndMetaInfo: [] as Array<DataAndMetaInformationLksgData>,
      listOfColumnIdentifierObjects: [] as Array<{ dataId: string; reportingPeriod: string }>,
      kpiDataObjects: new Map() as Map<string, kpiDataObject>,
      lksgDataModel: lksgDataModel,
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
     * Fetches all accepted LkSG datasets for the current company and converts them to the required frontend format.
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
     * @param kpiKey The field name of a kpi
     * @param kpiValue The corresponding value to the kpiKey
     * @param subcategory The sub category to which the kpi belongs
     * @param dataIdOfLksgDataset The value of the date kpi of an LkSG dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: object | string | Array<string> | number | null,
      subcategory: Subcategory,
      dataIdOfLksgDataset: string
    ): void {
      const kpi = subcategory.fields.filter((field) => field.name === kpiKey)[0];
      if (kpiKey === "totalRevenue" && typeof kpiValue === "number") {
        kpiValue = this.convertToMillions(kpiValue);
      }
      if (kpiKey === "industry" || kpiKey === "subcontractingCompaniesIndustries") {
        kpiValue = Array.isArray(kpiValue)
          ? kpiValue.map((naceCodeShort: string) => naceCodeMap.get(naceCodeShort)?.label ?? naceCodeShort)
          : naceCodeMap.get(kpiValue as string)?.label ?? kpiValue;
      }
      if (kpiKey === "subcontractingCompaniesCountries") {
        kpiValue = Array.isArray(kpiValue)
          ? kpiValue.map(
              (countryCodeShort: string) => getCountryNameFromCountryCode(countryCodeShort) ?? countryCodeShort
            )
          : getCountryNameFromCountryCode(kpiValue as string) ?? kpiValue;
      }
      kpiValue = kpi.options?.filter((option) => option.value === kpiValue)[0]?.label ?? kpiValue;

      const kpiData = {
        subcategoryKey: subcategory.name == "masterData" ? `_${subcategory.name}` : subcategory.name,
        subcategoryLabel: subcategory.label ? subcategory.label : subcategory.name,
        kpiKey: kpiKey,
        kpiLabel: kpi?.label ? kpi.label : kpiKey,
        kpiDescription: kpi?.description ? kpi.description : "",
        [dataIdOfLksgDataset]: kpiValue,
      } as kpiDataObject;

      this.kpiDataObjects.set(kpiKey, kpiData);
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
          for (const [areaKey, areaObject] of Object.entries(oneLksgDataset.data)) {
            for (const [subAreaKey, subAreaObject] of Object.entries(areaObject as object) as [string, object][]) {
              for (const [kpiKey, kpiValue] of Object.entries(subAreaObject) as [string, object][]) {
                const subcategory = lksgDataModel
                  .filter((area) => area.name === areaKey)[0]
                  .subcategories.filter((category) => category.name === subAreaKey)[0];
                this.createKpiDataObjects(kpiKey, kpiValue, subcategory, dataIdOfLksgDataset);
              }
            }
          }
        });
      }
      this.listOfColumnIdentifierObjects = sortReportingPeriodsToDisplayAsColumns(this.listOfColumnIdentifierObjects);
    },

    /**
     * Converts a number to millions with max two decimal places and adds "MM" at the end of the number.
     * @param inputNumber The number to convert
     * @returns a string with the converted number and "MM" at the end
     */
    convertToMillions(inputNumber: number): string {
      return `${(inputNumber / 1000000).toLocaleString("en-GB", { maximumFractionDigits: 2 })} MM`;
    },
  },
});
</script>
