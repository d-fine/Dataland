<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.EutaxonomyNonFinancials) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData" data-test="multipleReportsBanner">
    <ShowMultipleReportsBanner
      data-test="multipleReportsBanner"
      v-if="
        extractedReportsAndReportingPeriods &&
        extractedReportsAndReportingPeriods[0] &&
        extractedReportsAndReportingPeriods[1]
      "
      :reporting-periods="extractedReportsAndReportingPeriods[1]"
      :reports="extractedReportsAndReportingPeriods[0]"
    />
    <ThreeLayerTable
      data-test="ThreeLayerTableTest"
      :data-model="euTaxonomyForNonFinancialsDisplayDataModel"
      :data-and-meta-info="convertedDataAndMetaInfo"
      @data-converted="handleFinishedDataConversion"
      :format-value-for-display="formatValueForDisplay"
      :modal-column-headers="euTaxonomyForNonFinancialsModalColumnHeaders"
      :sort-by-subcategory-key="false"
      :unfold-subcategories="true"
    />
  </div>
</template>

<script lang="ts">
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import {
  type AmountWithCurrency,
  AssuranceDataAssuranceEnum,
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  DataTypeEnum,
  type EuTaxonomyDataForNonFinancials,
  FiscalYearDeviation,
} from "@clients/backend";
import type Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { type KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { euTaxonomyForNonFinancialsModalColumnHeaders } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsModalColumnHeaders";
import { euTaxonomyForNonFinancialsDisplayDataModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsDisplayDataModel";
import { DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsViewModel";
import { formatAmountWithCurrency } from "@/utils/Formatter";
import { roundNumber } from "@/utils/NumberConversionUtils";
import ShowMultipleReportsBanner from "@/components/resources/frameworkDataSearch/ShowMultipleReportsBanner.vue";
import type { CompanyReport } from "@clients/backend";

export default defineComponent({
  name: "EuTaxonomyForNonFinancialsPanel",
  components: { ThreeLayerTable, ShowMultipleReportsBanner },
  data() {
    return {
      DataTypeEnum,
      firstRender: true,
      waitingForData: true,
      waitingForReports: true,
      convertedDataAndMetaInfo: [] as Array<DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel>,
      euTaxonomyForNonFinancialsModalColumnHeaders,
      euTaxonomyForNonFinancialsDisplayDataModel,
      dataSet: null as EuTaxonomyDataForNonFinancials | null | undefined,
      dataAndMetaInfoSets: null as Array<DataAndMetaInformationEuTaxonomyDataForNonFinancials> | null | undefined,
      extractedReportsAndReportingPeriods: null as
        | [({ [p: string]: CompanyReport } | undefined)[], Array<string>]
        | null
        | undefined,
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.fetchEuTaxonomyData().catch((error) => console.log(error));
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.fetchEuTaxonomyData().catch((error) => console.log(error));
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    this.fetchEuTaxonomyData().catch((error) => console.log(error));
    this.firstRender = false;
  },

  methods: {
    humanizeString: humanizeStringOrNumber,
    /**
     * Fetches all accepted EU Taxonomy Non-Financial datasets for the current company and converts them to the required frontend format.
     */
    async fetchEuTaxonomyData() {
      try {
        let fetchedData: DataAndMetaInformationEuTaxonomyDataForNonFinancials[];
        this.waitingForData = true;
        const euTaxonomyForNonFinancialsDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getEuTaxonomyDataForNonFinancialsControllerApi();
        if (this.singleDataMetaInfoToDisplay) {
          const singleEuTaxonomyForNonFinancialsDataData = (
            await euTaxonomyForNonFinancialsDataControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(
              this.singleDataMetaInfoToDisplay.dataId,
            )
          ).data.data;
          fetchedData = [
            { metaInfo: this.singleDataMetaInfoToDisplay, data: singleEuTaxonomyForNonFinancialsDataData },
          ];
          this.dataSet = singleEuTaxonomyForNonFinancialsDataData;
          this.dataAndMetaInfoSets = fetchedData;
        } else {
          fetchedData = (
            await euTaxonomyForNonFinancialsDataControllerApi.getAllCompanyEuTaxonomyDataForNonFinancials(
              assertDefined(this.companyId),
            )
          ).data;
          this.dataAndMetaInfoSets = fetchedData;
        }
        this.extractedReportsAndReportingPeriods = this.extractReportsAndReportingPeriodsFromDataAndMetaInfoSets(
          this.dataAndMetaInfoSets,
        );
        this.convertedDataAndMetaInfo = fetchedData.map(
          (dataAndMetaInfo) => new DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel(dataAndMetaInfo),
        );
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Handles the ThreeLayerTableEvent of finishing the data conversion
     */
    handleFinishedDataConversion() {
      this.waitingForData = false;
    },

    /**
     * Checks if a KpiValue is an object with amount and/or currency
     * @param kpiValue the kpiValue that shall be checked
     * @returns a boolean based on the result of the check
     */
    hasKpiObjectAmountOrCurrency(kpiValue: KpiValue): boolean {
      return (
        typeof kpiValue === "object" &&
        (("amount" in kpiValue && (typeof kpiValue.amount === "number" || kpiValue.amount === null)) ||
          ("currency" in kpiValue && (typeof kpiValue.currency === "string" || kpiValue.currency === null)))
      );
    },

    /**
     * Checks if a KpiValue is a string with one of the Enum values of Assurance
     * @param kpiValue the kpiValue that shall be checked
     * @returns a boolean based on the result of the check
     */
    isKpiObjectAssuranceLevel(kpiValue: KpiValue): boolean {
      if (typeof kpiValue === "string") {
        return Object.values(AssuranceDataAssuranceEnum).includes(kpiValue as AssuranceDataAssuranceEnum);
      } else {
        return false;
      }
    },

    /**
     * Checks if a KpiValue is a string with one of the Enum values of FiscalYearDeviation
     * @param kpiValue the kpiValue that shall be checked
     * @returns a boolean based on the result of the check
     */
    isKpiObjectFiscalYearDeviation(kpiValue: KpiValue): boolean {
      if (typeof kpiValue === "string") {
        return Object.values(FiscalYearDeviation).includes(kpiValue as FiscalYearDeviation);
      } else {
        return false;
      }
    },

    /**
     * Formats KPI values for display
     * @param field the considered KPI field
     * @param kpiValueToFormat the value to be formatted
     * @returns the formatted value
     */
    formatValueForDisplay(field: Field, kpiValueToFormat: KpiValue): KpiValue {
      if (kpiValueToFormat == null) {
        return kpiValueToFormat;
      }
      if (this.isKpiObjectFiscalYearDeviation(kpiValueToFormat) || this.isKpiObjectAssuranceLevel(kpiValueToFormat)) {
        return humanizeStringOrNumber(kpiValueToFormat as string);
      }
      if (field.component == "PercentageFormField") {
        return roundNumber((kpiValueToFormat as number) * 100, 2);
      }
      if (this.hasKpiObjectAmountOrCurrency(kpiValueToFormat)) {
        return formatAmountWithCurrency(kpiValueToFormat as AmountWithCurrency);
      }
      return kpiValueToFormat;
    },

    /**
     * Extracts the reports and reporting periods for all data sets.
     * @param dataAndMetaInfoSets array of data sets includin meta information
     * @returns array containing an array of company reports and an array of the corresponding reporting periods
     * as strings
     */
    extractReportsAndReportingPeriodsFromDataAndMetaInfoSets(
      dataAndMetaInfoSets: Array<DataAndMetaInformationEuTaxonomyDataForNonFinancials>,
    ): [({ [p: string]: CompanyReport } | undefined)[], Array<string>] {
      const reportingPeriods = [];
      let tempReportingPeriod: string | undefined;
      for (const dataAndMetaInfoSet of dataAndMetaInfoSets) {
        tempReportingPeriod = dataAndMetaInfoSet.metaInfo.reportingPeriod;
        if (tempReportingPeriod) {
          reportingPeriods.push(tempReportingPeriod);
        }
      }
      const allReports = dataAndMetaInfoSets.map(
        (dataAndMetaInfoSet) => dataAndMetaInfoSet?.data?.general?.referencedReports,
      );
      this.waitingForReports = false;
      return [allReports, reportingPeriods];
    },
  },
});
</script>
