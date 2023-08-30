<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.EutaxonomyNonFinancials) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData">
    <ThreeLayerTable
      data-test="ThreeLayerTableTest"
      :data-model="euTaxonomyForNonFinancialsDisplayDataModel"
      :data-and-meta-info="convertedDataAndMetaInfo"
      @data-converted="handleFinishedDataConversion"
      :format-value-for-display="formatValueForDisplay"
      :modal-column-headers="euTaxonomyForNonFinancialsModalColumnHeaders"
      :sort-by-subcategory-key="false"
      unfold-on-load
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
import { formatAmountWithCurrency, formatPercentageNumber } from "@/utils/Formatting";

export default defineComponent({
  name: "EuTaxonomyForNonFinancialsPanel",
  components: { ThreeLayerTable },
  data() {
    return {
      DataTypeEnum,
      firstRender: true,
      waitingForData: true,
      convertedDataAndMetaInfo: [] as Array<DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel>,
      euTaxonomyForNonFinancialsModalColumnHeaders,
      euTaxonomyForNonFinancialsDisplayDataModel,
      namesOfFieldsToFormatAsPercentages: [
        "relativeShareInPercent",
        "enablingShare",
        "transitionalShare",
        "substantialContributionToClimateChangeMitigation",
        "substantialContributionToClimateChangeAdaption",
        "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources",
        "substantialContributionToTransitionToACircularEconomy",
        "substantialContributionToPollutionPreventionAndControl",
        "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems",
      ],
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
        } else {
          fetchedData = (
            await euTaxonomyForNonFinancialsDataControllerApi.getAllCompanyEuTaxonomyDataForNonFinancials(
              assertDefined(this.companyId),
            )
          ).data;
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
     * Formats KPI values for display
     * @param field the considered KPI field
     * @param kpiValueToFormat the value to be formatted
     * @returns the formatted value
     */
    formatValueForDisplay(field: Field, kpiValueToFormat: KpiValue): KpiValue {
      if (kpiValueToFormat == null) {
        return kpiValueToFormat;
      }
      if (this.namesOfFieldsToFormatAsPercentages.includes(field.name)) {
        return formatPercentageNumber(kpiValueToFormat as number);
      }
      if (this.hasKpiObjectAmountOrCurrency(kpiValueToFormat)) {
        return formatAmountWithCurrency(kpiValueToFormat as AmountWithCurrency);
      }
      return kpiValueToFormat;
    },
  },
});
</script>
