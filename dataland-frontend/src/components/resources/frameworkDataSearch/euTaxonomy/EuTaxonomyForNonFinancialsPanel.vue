<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.EutaxonomyNonFinancials) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData" data-test="multipleReportsBanner">
    <ShowMultipleReportsBanner
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
    />
  </div>
</template>

<script lang="ts">
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import {
  type AmountWithCurrency,
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  DataTypeEnum,
  type EuTaxonomyDataForNonFinancials,
} from "@clients/backend";
import type Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import { humanizeString } from "@/utils/StringHumanizer";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { type KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { euTaxonomyForNonFinancialsModalColumnHeaders } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsModalColumnHeaders";
import { euTaxonomyForNonFinancialsDisplayDataModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsDisplayDataModel";
import { DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsViewModel";
import { EnvironmentalObjective } from "@/api-models/EnvironmentalObjective";
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
      namesOfFieldsToFormatAsPercentages: ["relativeShareInPercent", "totalEnablingShare", "totalTransitionalShare"],
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
    humanizeString,
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
          console.log("if:");
          console.log(this.dataSet);
        } else {
          console.log("else");
          fetchedData = (
            await euTaxonomyForNonFinancialsDataControllerApi.getAllCompanyEuTaxonomyDataForNonFinancials(
              assertDefined(this.companyId),
            )
          ).data;
          this.dataAndMetaInfoSets = fetchedData;
          this.extractedReportsAndReportingPeriods = this.extractReportsAndReportingPeriodsFromDataAndMetaInfoSets(
            this.dataAndMetaInfoSets,
          );
          console.log(this.dataAndMetaInfoSets);
        }
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
     * Checks if a field name is included in the EnvironmentalObjectives enum.
     * @param fieldName is the field name to check for
     * @returns a boolean based on the result of the check
     */
    isFieldNameAmongEnvironmentalObjectives(fieldName: string): boolean {
      return Object.values(EnvironmentalObjective).includes(fieldName);
    },

    /**
     * Formats an AmountWithCurrency object by concatenating the amount and the currency.
     * @param amountWithCurrency the object that holds the amount and currency
     * @returns the resulting string from the concatenation
     */
    formatAmountWithCurrency(amountWithCurrency: AmountWithCurrency) {
      if (amountWithCurrency.amount == undefined) {
        return null;
      }
      return `${Math.round(amountWithCurrency.amount).toString()} ${amountWithCurrency.currency ?? ""}`;
    },

    /**
     * Formats a percentage number by rounding it to two decimals and afterward making it a string with a percent
     * symbol at the end.
     * @param relativeShareInPercent is the percentage number to round
     * @returns the resulting string
     */
    formatPercentageNumber(relativeShareInPercent: number) {
      return `${relativeShareInPercent.toFixed(2).toString()} %`;
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
      if (
        this.namesOfFieldsToFormatAsPercentages.includes(field.name) ||
        this.isFieldNameAmongEnvironmentalObjectives(field.name)
      ) {
        return this.formatPercentageNumber(kpiValueToFormat as number);
      }
      if (this.hasKpiObjectAmountOrCurrency(kpiValueToFormat)) {
        return this.formatAmountWithCurrency(kpiValueToFormat as AmountWithCurrency);
      }
      return kpiValueToFormat;
    },

    /**
     * Extracts the reports and reporting periods for all data sets.
     * @param dataAndMetaInfoSets array of data sets includin meta information
     */
    extractReportsAndReportingPeriodsFromDataAndMetaInfoSets(
      dataAndMetaInfoSets: Array<DataAndMetaInformationEuTaxonomyDataForNonFinancials>,
    ): [({ [p: string]: CompanyReport } | undefined)[], Array<string>] {
      const reportingPeriods = [];
      let tempReportingPeriod: string | undefined;
      for (let i = 0; i < dataAndMetaInfoSets.length; i++) {
        tempReportingPeriod = dataAndMetaInfoSets[i].metaInfo.reportingPeriod;
        if (tempReportingPeriod) {
          reportingPeriods.push(tempReportingPeriod);
        } else console.log("no reporting period given");
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
