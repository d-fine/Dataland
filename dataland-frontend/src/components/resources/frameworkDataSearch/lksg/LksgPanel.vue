<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading LkSG Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="mapOfKpiKeysToDataObjects.size > 0 && !waitingForData">
    <TwoLayerDataTable
      :arrayOfKpiDataObjects="Array.from(mapOfKpiKeysToDataObjects.values())"
      :list-of-reporting-periods-with-data-id="arrayOfReportingPeriodWithDataId"
      :modal-column-headers="lksgModalColumnHeaders"
    />
  </div>
</template>

<script lang="ts">
import { KpiDataObject, KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import TwoLayerDataTable from "@/components/resources/frameworkDataSearch/TwoLayerDataTable.vue";
import { lksgDataModel } from "@/components/resources/frameworkDataSearch/lksg/LksgDataModel";
import { ApiClientProvider } from "@/services/ApiClients";
import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import { Subcategory, Field } from "@/utils/GenericFrameworkTypes";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { DataAndMetaInformationLksgData, LksgData, LksgProcurementCategory } from "@clients/backend";
import Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { lksgModalColumnHeaders } from "@/components/resources/frameworkDataSearch/lksg/LksgModalColumnHeaders";
import { convertToMillions } from "@/utils/NumberConversionUtils";
import { convertNace } from "@/utils/NaceCodeConverter";

export default defineComponent({
  name: "LksgPanel",
  components: { TwoLayerDataTable: TwoLayerDataTable },
  data() {
    return {
      firstRender: true,
      waitingForData: true,
      lksgDataAndMetaInfo: [] as Array<DataAndMetaInformationLksgData>,
      arrayOfReportingPeriodWithDataId: [] as Array<ReportingPeriodOfDataSetWithId>,
      mapOfKpiKeysToDataObjects: new Map() as Map<string, KpiDataObject>,
      lksgModalColumnHeaders,
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.arrayOfReportingPeriodWithDataId = [];
      void this.fetchLksgData();
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.arrayOfReportingPeriodWithDataId = [];
        void this.fetchLksgData();
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    void this.fetchLksgData();
    this.firstRender = false;
  },
  methods: {
    /**
     * Fetches all accepted LkSG datasets for the current company and converts them to the required frontend format.
     */
    async fetchLksgData() {
      try {
        this.waitingForData = true;
        const lksgDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getLksgDataControllerApi();
        if (this.singleDataMetaInfoToDisplay) {
          const singleLksgData = (
            await lksgDataControllerApi.getCompanyAssociatedLksgData(this.singleDataMetaInfoToDisplay.dataId)
          ).data.data;

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
      kpiValue: KpiValue,
      subcategory: Subcategory,
      dataIdOfLksgDataset: string,
    ): void {
      const kpiField = assertDefined(subcategory.fields.find((field) => field.name === kpiKey));

      const kpiData = {
        subcategoryKey: subcategory.name == "masterData" ? `_${subcategory.name}` : subcategory.name,
        subcategoryLabel: subcategory.label ? subcategory.label : subcategory.name,
        kpiKey: kpiKey,
        kpiLabel: kpiField?.label ? kpiField.label : kpiKey,
        kpiDescription: kpiField?.description ? kpiField.description : "",
        kpiFormFieldComponent: kpiField?.component ?? "",
        content: { [dataIdOfLksgDataset]: this.reformatValueForDisplay(kpiField, kpiValue ?? "") },
      } as KpiDataObject;
      if (this.mapOfKpiKeysToDataObjects.has(kpiKey)) {
        Object.assign(kpiData.content, this.mapOfKpiKeysToDataObjects.get(kpiKey)?.content);
      }
      this.mapOfKpiKeysToDataObjects.set(kpiKey, kpiData);
    },

    /**
     * Retrieves and converts the stored array of LkSG datasets in order to make it displayable in the frontend.
     */
    convertLksgDataToFrontendFormat(): void {
      if (this.lksgDataAndMetaInfo.length) {
        this.lksgDataAndMetaInfo.forEach((currentLksgDataset: DataAndMetaInformationLksgData) => {
          const dataIdOfLksgDataset = currentLksgDataset.metaInfo?.dataId ?? "";
          const reportingPeriodOfLksgDataset = currentLksgDataset.metaInfo?.reportingPeriod ?? "";
          this.arrayOfReportingPeriodWithDataId.push({
            dataId: dataIdOfLksgDataset,
            reportingPeriod: reportingPeriodOfLksgDataset,
          });
          this.addKpisOfOneDatasetToTableModel(currentLksgDataset.data, dataIdOfLksgDataset);
        });
      }
      this.arrayOfReportingPeriodWithDataId = sortReportingPeriodsToDisplayAsColumns(
        this.arrayOfReportingPeriodWithDataId as ReportingPeriodOfDataSetWithId[],
      );
    },
    /**
     * Adds the kpis of an LkSG dataset to the model passed to the data table
     * @param lksgData the LkSG dataset to iterate over
     * @param dataId the datasets ID
     */
    addKpisOfOneDatasetToTableModel(lksgData: LksgData, dataId: string) {
      for (const [categoryKey, categoryObject] of Object.entries(lksgData) as [string, object | null]) {
        if (categoryObject == null) continue;
        for (const [subCategoryKey, subCategoryObject] of Object.entries(categoryObject as object) as [
          string,
          object | null,
        ][]) {
          if (subCategoryObject == null) continue;
          for (const [kpiKey, kpiValue] of Object.entries(subCategoryObject)) {
            const subcategory = assertDefined(
              lksgDataModel
                .find((category) => category.name === categoryKey)
                ?.subcategories.find((subCategory) => subCategory.name === subCategoryKey),
            );
            this.createKpiDataObjects(kpiKey, (kpiValue as KpiValue) ?? "", subcategory, dataId);
          }
        }
      }
    },

    /**
     * Converts a country code to a human readable value
     * @param kpiValue the value that should be reformated corresponding to its field
     * @returns the reformatted Country value ready for display
     */
    reformatCountriesValue(kpiValue: KpiValue) {
      return Array.isArray(kpiValue)
        ? kpiValue.map((countryCodeShort: string) => getCountryNameFromCountryCode(countryCodeShort))
        : getCountryNameFromCountryCode(kpiValue as string) ?? kpiValue;
    },

    /**
     * Generates a list of readable strings (or just a single one) combining suppliers and their associated countries
     * @param numberOfSuppliersPerCountryCode the map of number of suppliers and associated companies
     * from which strings are written
     * @returns the constructed collection of readable strings
     */
    generateReadableCombinationOfNumberOfSuppliersAndCountries(
      numberOfSuppliersPerCountryCode?: Map<string, number | undefined | null>,
    ) {
      if (numberOfSuppliersPerCountryCode != undefined) {
        const readableListOfSuppliersAndCountries = Array.from(numberOfSuppliersPerCountryCode.entries()).map(
          ([countryCode, numberOfSuppliers]) => {
            const countryName = getCountryNameFromCountryCode(countryCode);
            if (numberOfSuppliers != undefined) {
              return String(numberOfSuppliers) + " suppliers from " + countryName;
            } else {
              return "There are suppliers from " + countryName;
            }
          },
        );
        if (readableListOfSuppliersAndCountries.length > 1) {
          return readableListOfSuppliersAndCountries;
        } else {
          return readableListOfSuppliersAndCountries[0];
        }
      } else {
        return null;
      }
    },

    /**
     * Converts the map of ProcurementCategory and LksgProductCategory into an array for a proper handling of the
     * DetailsCompanyDataTable in the LksgCompanyDataTable (modal showing information related to Procurement Categories)
     * @param inputObject Map to convert to array
     * @returns The constructed map
     */
    reformatProcurementCategoriesValue(inputObject: Map<ProcurementCategoryType, LksgProcurementCategory> | null) {
      if (inputObject == null) return null;
      const inputObjectEntries = Object.entries(inputObject) as [ProcurementCategoryType, LksgProcurementCategory][];
      return inputObjectEntries.map((inputEntry: [ProcurementCategoryType, LksgProcurementCategory]) => {
        const [procurementCategoryType, lksgProcurementCategory] = inputEntry;
        const definitionsOfProductTypeOrService =
          lksgProcurementCategory.procuredProductTypesAndServicesNaceCodes.length > 1
            ? lksgProcurementCategory.procuredProductTypesAndServicesNaceCodes
            : lksgProcurementCategory.procuredProductTypesAndServicesNaceCodes[0] ?? "";

        return {
          procurementCategory: procurementCategoryType,
          procuredProductTypesAndServicesNaceCodes: convertNace(definitionsOfProductTypeOrService),
          suppliersAndCountries: this.generateReadableCombinationOfNumberOfSuppliersAndCountries(
            new Map(Object.entries(lksgProcurementCategory.numberOfSuppliersPerCountryCode ?? {})),
          ),
          percentageOfTotalProcurement:
            lksgProcurementCategory.percentageOfTotalProcurement != null
              ? String(lksgProcurementCategory.percentageOfTotalProcurement).concat(" %")
              : null,
        };
      });
    },

    /**
     *
     * @param kpiField the Field to which the value belongs
     * @param kpiValue the value that should be reformated corresponding to its field
     * @returns the reformatted value ready for display
     */
    reformatValueForDisplay(kpiField: Field, kpiValue: KpiValue): KpiValue {
      if (kpiField.name === "totalRevenue" && typeof kpiValue === "number") {
        kpiValue = convertToMillions(kpiValue);
      }
      if (kpiField.name === "industry" || kpiField.name === "subcontractingCompaniesIndustries") {
        kpiValue = convertNace(kpiValue);
      }
      if (kpiField.name.includes("Countries") && kpiField.component !== "YesNoFormField") {
        kpiValue = this.reformatCountriesValue(kpiValue);
      }
      if (kpiField.name === "productsServicesCategoriesPurchased") {
        kpiValue = this.reformatProcurementCategoriesValue(
          kpiValue as Map<ProcurementCategoryType, LksgProcurementCategory> | null,
        );
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
