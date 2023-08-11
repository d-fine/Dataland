<template>
  <div v-show="mapOfCategoryKeysToDataObjectArrays.size > 0">
    <DataTable tableClass="onlyHeaders">
      <Column
        headerStyle="width: 30vw;"
        headerClass="horizontal-headers-size first-horizontal-headers-size"
        header="KPIs"
      />
      <Column
        v-for="reportingPeriodWithDataId of arrayOfReportingPeriodWithDataId"
        headerClass="horizontal-headers-size"
        headerStyle="width: 30vw;"
        :header="reportingPeriodWithDataId.reportingPeriod"
        :key="reportingPeriodWithDataId.dataId"
      />
    </DataTable>
    <div
      v-for="(arrayOfKpiDataObjectsMapItem, index) in mapOfCategoryKeysToDataObjectArrays"
      :key="index"
      class="d-table-style"
    >
      <div v-if="shouldCategoryBeRendered(arrayOfKpiDataObjectsMapItem[0])">
        <div>
          <div class="pt-2 pl-2 pb-2 w-full d-cursor-pointer border-bottom-table p-2" @click="toggleExpansion(index)">
            <span
              :class="`p-badge badge-${colorOfCategory(arrayOfKpiDataObjectsMapItem[0])}`"
              :data-test="arrayOfKpiDataObjectsMapItem[0]"
              >{{ arrayOfKpiDataObjectsMapItem[0].toUpperCase() }}
            </span>
            <button v-if="!isExpanded(index)" class="pt-1 pr-3 d-cursor-pointer d-chevron-style">
              <span class="pr-1 pt-1 pi pi-chevron-right d-chevron-font"></span>
            </button>
            <button v-if="isExpanded(index)" class="pt-2 pr-3 d-cursor-pointer d-chevron-style">
              <span class="pr-1 pi pi-chevron-down d-chevron-font"></span>
            </button>
          </div>
        </div>
        <div v-show="isExpanded(index)">
          <TwoLayerDataTable
            :arrayOfKpiDataObjects="arrayOfKpiDataObjectsMapItem[1]"
            :list-of-reporting-periods-with-data-id="arrayOfReportingPeriodWithDataId"
            headerInputStyle="display: none;"
            :modal-column-headers="modalColumnHeaders"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { KpiDataObject, KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import TwoLayerDataTable from "@/components/resources/frameworkDataSearch/TwoLayerDataTable.vue";
import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import {Category, DataAndMetaInformation, Field, FrameworkData, Subcategory} from "@/utils/GenericFrameworkTypes";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { defineComponent } from "vue";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import {
    AssuranceData,
    CompanyReport,
    CreditInstitutionKpis,
    DataMetaInformation,
    EligibilityKpis,
    EuTaxonomyDataForFinancials,
    EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
    FiscalYearDeviation,
    InsuranceKpis,
    InvestmentFirmKpis,
    LksgData,
    LksgEnvironmental,
    LksgGeneral,
    LksgGovernance,
    LksgSocial,
    P2pAmmonia,
    P2pAutomotive, P2pCement, P2pElectricityGeneration, P2pFreightTransportByRoad,
    P2pGeneral, P2pHvcPlastics, P2pLivestockFarming, P2pRealEstate, P2pSteel,
    PathwaysToParisData,
    SfdrData,
    SfdrEnvironmental,
    SfdrSocial,
    SmeData,
    SmeGeneral,
    SmeInsurances,
    SmePower,
    SmeProduction,
    YesNo,
    YesNoNa
} from "@clients/backend";

export default defineComponent({
  name: "ThreeLayerTable",

  components: { TwoLayerDataTable, DataTable, Column },
  data() {
    return {
      expandedGroup: [0],
      resultKpiData: null as KpiDataObject,
      arrayOfReportingPeriodWithDataId: [] as Array<ReportingPeriodOfDataSetWithId>,
      mapOfKpiKeysToDataObjects: new Map() as Map<string, KpiDataObject>,
      mapOfCategoryKeysToDataObjectArrays: new Map() as Map<string, Array<KpiDataObject>>,
      importantCategoryKeys: ["general"],
      importantSubcategoryKeys: ["general", "basicInformation", "masterData"],
    };
  },
  props: {
    dataModel: {
      type: Array as () => Array<Category>,
      required: true,
    },
    dataAndMetaInfo: {
      type: Array as () => Array<DataAndMetaInformationViewModel>,
      required: true,
    },
    formatValueForDisplay: {
      type: Function as () => (field: Field, value: KpiValue) => KpiValue,
      default: (field: Field, value: KpiValue): KpiValue => value,
    },
    modalColumnHeaders: {
      type: Object,
      default: () => ({}),
    },
  },
  watch: {
    dataAndMetaInfo() {
      if (this.dataAndMetaInfo.length > 0) {
        this.convertDataToFrontendFormat();
      }
    },
  },
  emits: ["dataConverted"],
  methods: {
    /**
     * Creates kpi data objects to pass them to the data table.
     * @param kpiKey The field name of a kpi
     * @param kpiValue The corresponding value to the kpiKey
     * @param subcategory The sub category to which the kpi belongs
     * @param category category to which the kpi belongs to
     * @param dataId The value of the date kpi of an LkSG dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: KpiValue,
      subcategory: Subcategory,
      category: Category,
      dataId: string,
    ): void {
      const kpiField = assertDefined(subcategory.fields.find((field) => field.name === kpiKey));
      const kpiData = {
        categoryKey: this.importantCategoryKeys.includes(category.name) ? `_${category.name}` : category.name,
        categoryLabel: category.label ? category.label : category.name,
        subcategoryKey: this.importantSubcategoryKeys.includes(subcategory.name)
          ? `_${subcategory.name}`
          : subcategory.name,
        subcategoryLabel: subcategory.label ? subcategory.label : subcategory.name,
        kpiKey: kpiKey,
        kpiLabel: kpiField?.label ? kpiField.label : kpiKey,
        kpiDescription: kpiField?.description ? kpiField.description : "",
        kpiFormFieldComponent: kpiField?.component ?? "",
        content: { [dataId]: this.formatValueForDisplay(kpiField, kpiValue) ?? "" },
      } as KpiDataObject;
      if (this.mapOfKpiKeysToDataObjects.has(kpiKey)) {
        Object.assign(kpiData.content, this.mapOfKpiKeysToDataObjects.get(kpiKey)?.content);
      }
      this.mapOfKpiKeysToDataObjects.set(kpiKey, kpiData);
      this.resultKpiData = kpiData;
    },
    /**
     * Retrieves and converts the stored array of datasets in order to make it displayable in the frontend.
     */
    convertDataToFrontendFormat(): void {
      this.arrayOfReportingPeriodWithDataId = [];
      if (this.dataAndMetaInfo.length) {
        this.dataAndMetaInfo.forEach((currentDataset) => {
          const dataId = currentDataset.metaInfo?.dataId ?? "";
          const reportingPeriod = currentDataset.metaInfo?.reportingPeriod ?? "";
          this.arrayOfReportingPeriodWithDataId.push({
            dataId: dataId,
            reportingPeriod: reportingPeriod,
          });
          for (const [categoryKey, categoryObject] of Object.entries(currentDataset.data) as [string, object] | null) {
            if (categoryObject == null) continue;
            const listOfDataObjects: Array<KpiDataObject> = [];
            const frameworkCategoryData = assertDefined(
              this.dataModel.find((category) => category.name === categoryKey),
            );
            this.iterateThroughSubcategories(
              categoryObject,
              categoryKey,
              frameworkCategoryData,
              dataId,
              listOfDataObjects,
              currentDataset.data,
            );

            this.mapOfCategoryKeysToDataObjectArrays.set(frameworkCategoryData.label, listOfDataObjects);
          }
        });
      }
      this.arrayOfReportingPeriodWithDataId = sortReportingPeriodsToDisplayAsColumns(
        this.arrayOfReportingPeriodWithDataId as ReportingPeriodOfDataSetWithId[],
      );
      this.$emit("dataConverted");
    },
    /**
     * Iterates through all subcategories of a category
     * @param categoryObject the data object of the framework's category
     * @param categoryKey the key of the corresponding framework's category
     * @param frameworkCategoryData  the category object of the framework's category
     * @param dataId  the ID of the dataset
     * @param listOfDataObjects a map containing the category and it's corresponding Kpis
     * @param currentDataset dataset for which the show if conditions should be checked
     */
    iterateThroughSubcategories(
      categoryObject,
      categoryKey,
      frameworkCategoryData: Category,
      dataId: string,
      listOfDataObjects: Array<KpiDataObject>,
      currentDataset: FrameworkViewModel,
    ) {
      for (const [subCategoryKey, subCategoryObject] of Object.entries(categoryObject as object) as [
        string,
        object | null,
      ][]) {
        if (subCategoryObject == null) continue;
        this.iterateThroughSubcategoryKpis(
          subCategoryObject,
          categoryKey,
          subCategoryKey,
          frameworkCategoryData,
          dataId,
          listOfDataObjects,
          currentDataset.toApiModel(),
        );
      }
    },
    /**
     * Builds the result Kpi Data Object and adds it to the result list
     * @param subCategoryObject the data object of the framework's subcategory
     * @param categoryKey the key of the corresponding framework's category
     * @param subCategoryKey the key of the corresponding framework's subcategory
     * @param frameworkCategoryData the category object of the framework's category
     * @param dataId the ID of the dataset
     * @param listOfDataObjects a map containing the category and it's corresponding Kpis
     * @param currentDataset dataset for which the show if conditions should be checked
     */
    iterateThroughSubcategoryKpis(
      subCategoryObject: object,
      categoryKey,
      subCategoryKey: string,
      frameworkCategoryData: Category,
      dataId: string,
      listOfDataObjects: Array<KpiDataObject>,
      currentDataset: FrameworkData,
    ) {
      for (const [kpiKey, kpiValue] of Object.entries(subCategoryObject) as [string, object] | null) {
        const subcategory = assertDefined(
          frameworkCategoryData.subcategories.find((subCategory) => subCategory.name === subCategoryKey),
        );
        const field = assertDefined(subcategory.fields.find((field) => field.name == kpiKey));

        if (field.showIf(currentDataset)) {
          this.createKpiDataObjects(kpiKey as string, kpiValue as KpiValue, subcategory, frameworkCategoryData, dataId);
          listOfDataObjects.push(this.resultKpiData);
        }
      }
    },
    /**
     * Checks whether a given category shall be displayed for at least one of the datasets to display
     * @param categoryName The name of the category to check
     * @returns true if category shall be displayed, else false
     */
    shouldCategoryBeRendered(categoryName: string): boolean {
      const category = assertDefined(this.dataModel.find((category) => category.label === categoryName));
      return this.dataAndMetaInfo.some((dataAndMetaInfo) => category.showIf(dataAndMetaInfo.data.toApiModel()));
    },
    /**
     * Retrieves the color for a given category from Data Model
     * @param categoryName The name of the category whose color is searched
     * @returns color as string
     */
    colorOfCategory(categoryName: string): string {
      return assertDefined(this.dataModel.find((category) => category.label === categoryName)).color;
    },
    /**
     * Checks whether an element is expanded or not
     * @param key element for which the check should be run
     * @returns if the element is expanded or not
     */
    isExpanded(key: number) {
      return this.expandedGroup.indexOf(key) !== -1;
    },
    /**
     * Expands and collapses an item
     * @param key element for which the check should be run
     */
    toggleExpansion(key: number) {
      if (this.isExpanded(key)) this.expandedGroup.splice(this.expandedGroup.indexOf(key), 1);
      else this.expandedGroup.push(key);
    },
  },
});

export interface DataAndMetaInformationViewModel {
    metaInfo: DataMetaInformation;
    data: FrameworkViewModel;
}

export interface FrameworkViewModel {
    constructor(apiModel: FrameworkData)
    toApiModel(): FrameworkData
}

class EuTaxonomyDataForFinancialsViewModel implements FrameworkViewModel, EuTaxonomyDataForFinancials {
    financialServicesTypes?: Array<EuTaxonomyDataForFinancialsFinancialServicesTypesEnum>;
    eligibilityKpis?: { [key: string]: EligibilityKpis; };
    creditInstitutionKpis?: CreditInstitutionKpis;
    investmentFirmKpis?: InvestmentFirmKpis;
    insuranceKpis?: InsuranceKpis;
    fiscalYearDeviation?: FiscalYearDeviation;
    fiscalYearEnd?: string;
    scopeOfEntities?: YesNoNa;
    reportingObligation?: YesNo;
    activityLevelReporting?: YesNo;
    assurance?: AssuranceData;
    numberOfEmployees?: number;
    referencedReports?: { [key: string]: CompanyReport; };

    constructor(apiModel: EuTaxonomyDataForFinancials) {
        this.financialServicesTypes = apiModel.financialServicesTypes;
        this.eligibilityKpis = apiModel.eligibilityKpis;
        this.creditInstitutionKpis = apiModel.creditInstitutionKpis;
        this.investmentFirmKpis = apiModel.investmentFirmKpis;
        this.insuranceKpis = apiModel.insuranceKpis;
        this.fiscalYearDeviation = apiModel.fiscalYearDeviation;
        this.fiscalYearEnd = apiModel.fiscalYearEnd;
        this.scopeOfEntities = apiModel.scopeOfEntities;
        this.reportingObligation = apiModel.reportingObligation;
        this.activityLevelReporting = apiModel.activityLevelReporting;
        this.assurance = apiModel.assurance;
        this.numberOfEmployees = apiModel.numberOfEmployees;
        this.referencedReports = apiModel.referencedReports;
    }

    toApiModel(): EuTaxonomyDataForFinancials {
        return this;
    }
}

class LksgDataViewModel implements FrameworkViewModel, LksgData {
    general: LksgGeneral;
    governance?: LksgGovernance;
    social?: LksgSocial;
    environmental?: LksgEnvironmental;

    constructor(apiModel: LksgData) {
        this.general = apiModel.general;
        this.governance = apiModel.governance;
        this.social = apiModel.social;
        this.environmental = apiModel.environmental;
    }

    toApiModel(): LksgData {
        return this;
    }
}

class SfdrDataViewModel implements FrameworkViewModel, SfdrData {
    social?: SfdrSocial;
    environmental?: SfdrEnvironmental;
    referencedReports?: { [key: string]: CompanyReport; };

    constructor(apiModel: SfdrData) {
        this.social = apiModel.social;
        this.environmental = apiModel.environmental;
        this.referencedReports = apiModel.referencedReports;
    }

    toApiModel(): SfdrData {
        return this;
    }
}

class SmeDataViewModel implements FrameworkViewModel, SmeData {
    general: SmeGeneral;
    production?: SmeProduction;
    power?: SmePower;
    insurances?: SmeInsurances;

    constructor(apiModel: SmeData) {
        this.general = apiModel.general;
        this.production = apiModel.production;
        this.power = apiModel.power;
        this.insurances = apiModel.insurances;
    }

    toApiModel(): SmeData {
        return this;
    }
}

class PathwaysToParisDataViewModel implements FrameworkViewModel, PathwaysToParisData {
    general: P2pGeneral;
    ammonia?: P2pAmmonia;
    automotive?: P2pAutomotive;
    hvcPlastics?: P2pHvcPlastics;
    commercialRealEstate?: P2pRealEstate;
    residentialRealEstate?: P2pRealEstate;
    steel?: P2pSteel;
    freightTransportByRoad?: P2pFreightTransportByRoad;
    electricityGeneration?: P2pElectricityGeneration;
    livestockFarming?: P2pLivestockFarming;
    cement?: P2pCement;

    constructor(apiModel: PathwaysToParisData) {
        this.general = apiModel.general;
        this.ammonia = apiModel.ammonia;
        this.automotive = apiModel.automotive;
        this.hvcPlastics = apiModel.hvcPlastics;
        this.commercialRealEstate = apiModel.commercialRealEstate;
        this.residentialRealEstate = apiModel.residentialRealEstate;
        this.steel = apiModel.steel;
        this.freightTransportByRoad = apiModel.freightTransportByRoad;
        this.electricityGeneration = apiModel.electricityGeneration;
        this.livestockFarming = apiModel.livestockFarming;
        this.cement = apiModel.cement;
    }

    toApiModel() {
        return this;
    }
}

</script>
<style scoped lang="scss">
.d-table-style {
  font-size: 16px;
  text-align: left;
  background-color: #f6f5ef;
}

.d-chevron-style {
  float: right;
  border: none;
  background-color: #f6f5ef;
}

.d-chevron-font {
  color: #e67f3f;
  font-size: 14px;
}
</style>
