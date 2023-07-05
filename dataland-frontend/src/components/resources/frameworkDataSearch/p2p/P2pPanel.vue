<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading LkSG Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="mapOfKpiKeysToDataObjects.size > 0 && !waitingForData">
    <LksgCompanyDataTable
      :arrayOfKpiDataObjects="Array.from(mapOfKpiKeysToDataObjects.values())"
      :list-of-reporting-periods-with-data-id="listOfDataSetReportingPeriods"
    />
  </div>
</template>

<script lang="ts">
import { naceCodeMap } from "@/components/forms/parts/elements/derived/NaceCodeTree";
import { KpiDataObject, KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import LksgCompanyDataTable from "@/components/resources/frameworkDataSearch/lksg/LksgCompanyDataTable.vue";
import { p2pDataModel } from "@/components/resources/frameworkDataSearch/p2p/P2pDataModel";
import { ApiClientProvider } from "@/services/ApiClients";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";
import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import { Field, Subcategory } from "@/utils/GenericFrameworkTypes";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { DataAndMetaInformationPathwaysToParisData } from "@clients/backend";
import Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";

export default defineComponent({
  name: "P2pPanel",
  components: { LksgCompanyDataTable },
  data() {
    return {
      firstRender: true,
      waitingForData: true,
      p2pDataAndMetaInfo: [] as Array<DataAndMetaInformationPathwaysToParisData>,
      listOfDataSetReportingPeriods: [] as Array<ReportingPeriodOfDataSetWithId>,
      mapOfKpiKeysToDataObjects: new Map() as Map<string, KpiDataObject>,
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.listOfDataSetReportingPeriods = [];
      void this.fetchP2pData();
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.listOfDataSetReportingPeriods = [];
        void this.fetchP2pData();
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    void this.fetchP2pData();
    this.firstRender = false;
  },
  methods: {
    /**
     * Fetches all accepted P2P datasets for the current company and converts them to the required frontend format.
     */
    async fetchP2pData() {
      try {
        this.waitingForData = true;
        const p2pDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getP2pDataControllerApi();
        if (this.singleDataMetaInfoToDisplay) {
          const singleP2pData = (
            await p2pDataControllerApi.getCompanyAssociatedP2pData(this.singleDataMetaInfoToDisplay.dataId)
          ).data.data;

          this.p2pDataAndMetaInfo = [{ metaInfo: this.singleDataMetaInfoToDisplay, data: singleP2pData }];
        } else {
          this.p2pDataAndMetaInfo = (
            await p2pDataControllerApi.getAllCompanyP2pData(assertDefined(this.companyId))
          ).data;
        }
        this.convertP2pDataToFrontendFormat();
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
      kpiValue: KpiValue,
      subcategory: Subcategory,
      dataIdOfLksgDataset: string
    ): void {
      const kpiField = assertDefined(subcategory.fields.find((field) => field.name === kpiKey));

      const kpiData = {
        subcategoryKey: subcategory.name == "masterData" ? `_${subcategory.name}` : subcategory.name,
        subcategoryLabel: subcategory.label ? subcategory.label : subcategory.name,
        kpiKey: kpiKey,
        kpiLabel: kpiField?.label ? kpiField.label : kpiKey,
        kpiDescription: kpiField?.description ? kpiField.description : "",
        kpiFormFieldComponent: kpiField?.component ?? "",
        content: { [dataIdOfLksgDataset]: this.reformatValueForDisplay(kpiField, kpiValue) },
      } as KpiDataObject;
      if (this.mapOfKpiKeysToDataObjects.has(kpiKey)) {
        Object.assign(kpiData.content, this.mapOfKpiKeysToDataObjects.get(kpiKey)?.content);
      }
      this.mapOfKpiKeysToDataObjects.set(kpiKey, kpiData);
    },

    /**
     * Retrieves and converts the stored array of LkSG datasets in order to make it displayable in the frontend.
     */
    convertP2pDataToFrontendFormat(): void {
      if (this.p2pDataAndMetaInfo.length) {
        this.p2pDataAndMetaInfo.forEach((oneP2pDataset: DataAndMetaInformationPathwaysToParisData) => {
          const dataIdOfP2pDataset = oneP2pDataset.metaInfo?.dataId ?? "";
          const reportingPeriodOfLksgDataset = oneP2pDataset.metaInfo?.reportingPeriod ?? "";
          this.listOfDataSetReportingPeriods.push({
            dataId: dataIdOfP2pDataset,
            reportingPeriod: reportingPeriodOfLksgDataset,
          });
          for (const [categoryKey, categoryObject] of Object.entries(oneP2pDataset.data)) {
            for (const [subCategoryKey, subCategoryObject] of Object.entries(categoryObject as object) as [
              string,
              object
            ][]) {
              for (const [kpiKey, kpiValue] of Object.entries(subCategoryObject)) {
                const subcategory = assertDefined(
                  p2pDataModel
                    .find((category) => category.name === categoryKey)
                    ?.subcategories.find((subCategory) => subCategory.name === subCategoryKey)
                );
                this.createKpiDataObjects(kpiKey, kpiValue as KpiValue, subcategory, dataIdOfP2pDataset);
              }
            }
          }
        });
      }
      this.listOfDataSetReportingPeriods = sortReportingPeriodsToDisplayAsColumns(
        this.listOfDataSetReportingPeriods as ReportingPeriodOfDataSetWithId[]
      );
    },

    /**
     * Converts a number to millions with max two decimal places and adds "MM" at the end of the number.
     * @param inputNumber The number to convert
     * @returns a string with the converted number and "MM" at the end
     */
    convertToMillions(inputNumber: number): string {
      return `${(inputNumber / 1000000).toLocaleString("en-GB", { maximumFractionDigits: 2 })} MM`;
    },

    /**
     * Converts a nace code to a human readable value
     * @param kpiValue the value that should be reformated corresponding to its field
     * @returns the reformatted Country value ready for display
     */
    reformatIndustriesValue(kpiValue: KpiValue) {
      return Array.isArray(kpiValue)
        ? kpiValue.map((naceCodeShort: string) => naceCodeMap.get(naceCodeShort)?.label ?? naceCodeShort)
        : naceCodeMap.get(kpiValue as string)?.label ?? kpiValue;
    },

    /**
     * Converts a country code to a human readable value
     * @param kpiValue the value that should be reformated corresponding to its field
     * @returns the reformatted Country value ready for display
     */
    reformatCountriesValue(kpiValue: KpiValue) {
      return Array.isArray(kpiValue)
        ? kpiValue.map(
            (countryCodeShort: string) => getCountryNameFromCountryCode(countryCodeShort) ?? countryCodeShort
          )
        : getCountryNameFromCountryCode(kpiValue as string) ?? kpiValue;
    },

    /**
     *
     * @param kpiField the Field to which the value belongs
     * @param kpiValue the value that should be reformated corresponding to its field
     * @returns the reformatted value ready for display
     */
    reformatValueForDisplay(kpiField: Field, kpiValue: KpiValue): KpiValue {
      if (kpiField.name === "totalRevenue" && typeof kpiValue === "number") {
        kpiValue = this.convertToMillions(kpiValue);
      }
      if (kpiField.name === "industry" || kpiField.name === "subcontractingCompaniesIndustries") {
        kpiValue = this.reformatIndustriesValue(kpiValue);
      }
      if (kpiField.name.includes("Countries") && kpiField.component !== "YesNoFormField") {
        kpiValue = this.reformatCountriesValue(kpiValue);
      }

      let returnValue;

      if (kpiField.options?.length) {
        const filteredOption = kpiField.options.find((option: DropdownOption) => option.value === kpiValue);
        if (filteredOption) returnValue = filteredOption.label;
      }

      return returnValue ?? kpiValue;
    },
  },
});
</script>
