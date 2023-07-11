<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading P2P Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="mapOfKpiKeysToDataObjects.size > 0 && !waitingForData">
    <DataTable>
      <Column bodyClass="headers-bg" headerStyle="width: 30vw;" headerClass="horizontal-headers-size" header="KPIs">
      </Column>
      <Column
        v-for="reportingPeriodWithDataId of listOfDataSetReportingPeriods"
        headerClass="horizontal-headers-size"
        headerStyle="width: 30vw;"
        :field="reportingPeriodWithDataId.dataId"
        :header="reportingPeriodWithDataId.reportingPeriod"
        :key="reportingPeriodWithDataId.dataId"
      >
      </Column>
    </DataTable>
    <div v-for="(arrayOfKpiDataObject, index) in mapOfKpiKeysToDataObjectsArrays" :key="index" class="d-table-style">
      <div v-if="shouldCategoryBeRendered(arrayOfKpiDataObject[0])">
        <!--//TODO fix the height of the category row -->
        <div class="w-full d-dataset-toggle" @click="toggleExpansion(index)">
          <div >
            <span :class="`p-badge badge-${colorOfCategory(arrayOfKpiDataObject[0])}`">{{ arrayOfKpiDataObject[0].toUpperCase() }}</span>
            <button style="float: right;">Tsdsdsd</button>
          </div>
        </div>
        <div v-show="isExpanded(index)">
          <P2pCompanyDataTable
            :arrayOfKpiDataObjects="arrayOfKpiDataObject[1]"
            :list-of-reporting-periods-with-data-id="listOfDataSetReportingPeriods"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { naceCodeMap } from "@/components/forms/parts/elements/derived/NaceCodeTree";
import { KpiDataObject, KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import P2pCompanyDataTable from "@/components/resources/frameworkDataSearch/p2p/P2pCompanyDataTable.vue";
import { p2pDataModel } from "@/components/resources/frameworkDataSearch/p2p/P2pDataModel";
import { ApiClientProvider } from "@/services/ApiClients";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";
import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import { Category, Field, Subcategory } from "@/utils/GenericFrameworkTypes";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { DataAndMetaInformationPathwaysToParisData, PathwaysToParisData } from "@clients/backend";
import Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import Column from "primevue/column";
import DataTable from "primevue/datatable";

export default defineComponent({
  name: "P2pPanel",
  components: { P2pCompanyDataTable, DataTable, Column },
  data() {
    return {
      firstRender: true,
      waitingForData: true,
      testData: null as PathwaysToParisData | null | undefined,
      resultKpiData: null as KpiDataObject,
      categoryName: String(),
      p2pDataAndMetaInfo: [] as Array<DataAndMetaInformationPathwaysToParisData>,
      listOfDataSetReportingPeriods: [] as Array<ReportingPeriodOfDataSetWithId>,
      mapOfKpiKeysToDataObjects: new Map() as Map<string, KpiDataObject>,
      listOfDataObjects: [] as Array<KpiDataObject>,
      mapOfKpiKeysToDataObjectsArrays: new Map() as Map<string, Array<KpiDataObject>>,
      expandedGroup: [],
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.listOfDataSetReportingPeriods = [];
      this.fetchP2pData().catch((error) => console.log(error));
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.listOfDataSetReportingPeriods = [];
        this.fetchP2pData().catch((error) => console.log(error));
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    this.fetchP2pData().catch((error) => console.log(error));
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
          this.testData = singleP2pData;
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
     * @param category category to which the kpi belongs to
     * @param dataIdOfP2pDataset The value of the date kpi of an LkSG dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: KpiValue,
      subcategory: Subcategory,
      category: Category,
      dataIdOfP2pDataset: string
    ): void {
      const kpiField = assertDefined(subcategory.fields.find((field) => field.name === kpiKey));
      const kpiData = {
        categoryKey: category.name == "general" ? `_${category.name}` : category.name,
        categoryLabel: category.label ? category.label : category.name,
        subcategoryKey: subcategory.name == "general" ? `_${subcategory.name}` : subcategory.name,
        subcategoryLabel: subcategory.label ? subcategory.label : subcategory.name,
        kpiKey: kpiKey,
        kpiLabel: kpiField?.label ? kpiField.label : kpiKey,
        kpiDescription: kpiField?.description ? kpiField.description : "",
        kpiFormFieldComponent: kpiField?.component ?? "",
        content: { [dataIdOfP2pDataset]: this.reformatValueForDisplay(kpiField, kpiValue) },
      } as KpiDataObject;
      if (this.mapOfKpiKeysToDataObjects.has(kpiKey)) {
        Object.assign(kpiData.content, this.mapOfKpiKeysToDataObjects.get(kpiKey)?.content);
      }
      this.mapOfKpiKeysToDataObjects.set(kpiKey, kpiData);
      this.resultKpiData = kpiData;
    },
    /**
     * Retrieves and converts the stored array of LkSG datasets in order to make it displayable in the frontend.
     */
    convertP2pDataToFrontendFormat(): void {
      if (this.p2pDataAndMetaInfo.length) {
        this.p2pDataAndMetaInfo.forEach((oneP2pDataset: DataAndMetaInformationPathwaysToParisData) => {
          const dataIdOfP2pDataset = oneP2pDataset.metaInfo?.dataId ?? "";
          const reportingPeriodOfP2pDataset = oneP2pDataset.metaInfo?.reportingPeriod ?? "";
          this.listOfDataSetReportingPeriods.push({
            dataId: dataIdOfP2pDataset,
            reportingPeriod: reportingPeriodOfP2pDataset,
          });
          for (const [categoryKey, categoryObject] of Object.entries(oneP2pDataset.data) as [string, object] | null) {
            if (categoryObject == null) continue;
            this.listOfDataObjects = [];
            //categories einsammeln
            for (const [subCategoryKey, subCategoryObject] of Object.entries(categoryObject as object) as [
              string,
              object | null
            ][]) {
              if (subCategoryObject == null) continue;
              for (const [kpiKey, kpiValue] of Object.entries(subCategoryObject) as [string, object] | null) {
                if (kpiValue == null) continue;
                const subcategory = assertDefined(
                  p2pDataModel
                    .find((category) => category.name === categoryKey)
                    ?.subcategories.find((subCategory) => subCategory.name === subCategoryKey)
                );
                const categoryResult = assertDefined(p2pDataModel.find((category) => category.name === categoryKey));
                this.categoryName = categoryResult.label.toString();
                const field = assertDefined(subcategory.fields.find((field) => field.name == kpiKey));
                if (
                  this.p2pDataAndMetaInfo
                    .map((dataAndMetaInfo) => dataAndMetaInfo.data)
                    .some((singleP2pData) => field.showIf(singleP2pData))
                ) {
                  this.createKpiDataObjects(
                    kpiKey as string,
                    kpiValue as KpiValue,
                    subcategory,
                    categoryResult,
                    dataIdOfP2pDataset
                  );
                  this.listOfDataObjects.push(this.resultKpiData);
                }
              }
            }

            this.mapOfKpiKeysToDataObjectsArrays.set(this.categoryName, this.listOfDataObjects);
          }
          console.log(this.mapOfKpiKeysToDataObjectsArrays);
          const test = this.mapOfKpiKeysToDataObjectsArrays.get(this.categoryName);
          console.log(test);
        });
      }
      //TODO check if the old map mapOfKpiKeysToDataObjects is still necessary
      this.listOfDataSetReportingPeriods = sortReportingPeriodsToDisplayAsColumns(
        this.listOfDataSetReportingPeriods as ReportingPeriodOfDataSetWithId[]
      );
    },

    /**
     * Checks whether a given category shall be displayed for at least one of the P2P datasets to display
     * @param categoryName The name of the category to check
     * @returns true if category shall be displayed, else false
     */
    shouldCategoryBeRendered(categoryName: string): boolean {
      const category = assertDefined(p2pDataModel.find((category) => category.label === categoryName));
      return this.p2pDataAndMetaInfo
        .map((dataAndMetaInfo) => dataAndMetaInfo.data)
        .some((singleP2pData) => category.showIf(singleP2pData));
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
     * Retrieves the color for a given category from P2P Data Model
     * @param categoryName The name of the category whose color is searched
     * @returns color as string
     */
    colorOfCategory(categoryName: string): string {
      return assertDefined(p2pDataModel.find((category) => category.label === categoryName)).color;
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
    /**
     *
     * @param key element for which the check should be run
     * @returns if the element is expanded or not
     */
    isExpanded(key: string) {
      return this.expandedGroup.indexOf(key) !== -1;
    },
    /**
     *
     * @param key element for which the check should be run
     */
    toggleExpansion(key: string) {
      if (this.isExpanded(key)) this.expandedGroup.splice(this.expandedGroup.indexOf(key), 1);
      else this.expandedGroup.push(key);
    },
  },
});
</script>
<style scoped lang="scss">
.d-dataset-toggle {
  cursor: pointer;
}
.d-table-style {
  font-size: 16px;
  text-align: left;
  background-color: #f6f5ef;
}
//TODO extract styles to css classes, extract inline style to classes, remove unused code and check if there are already classes which have the styles needed
</style>
