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
    />
  </div>
</template>

<script lang="ts">
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import {
  type AmountWithCurrency,
  type DataAndMetaInformationNewEuTaxonomyDataForNonFinancials,
  DataTypeEnum,
} from "@clients/backend";
import type Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import { humanizeString } from "@/utils/StringHumanizer";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { type KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { newEuTaxonomyForNonFinancialsModalColumnHeaders } from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsModalColumnHeaders";
import { newEuTaxonomyForNonFinancialsDisplayDataModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsDisplayDataModel";
import { DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsViewModel";
import { EnvironmentalObjective } from "@/api-models/EnvironmentalObjective";

export default defineComponent({
  name: "NewEuTaxonomyForNonFinancialsPanel",
  components: { ThreeLayerTable },
  data() {
    return {
      DataTypeEnum,
      firstRender: true,
      waitingForData: true,
      convertedDataAndMetaInfo: [] as Array<DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel>,
      euTaxonomyForNonFinancialsModalColumnHeaders: newEuTaxonomyForNonFinancialsModalColumnHeaders,
      euTaxonomyForNonFinancialsDisplayDataModel: newEuTaxonomyForNonFinancialsDisplayDataModel,
      namesOfFieldsToFormatAsPercentages: ["relativeShareInPercent", "totalEnablingShare", "totalTransitionalShare"],
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.fetchNewEuTaxonomyData().catch((error) => console.log(error));
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.fetchNewEuTaxonomyData().catch((error) => console.log(error));
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    this.fetchNewEuTaxonomyData().catch((error) => console.log(error));
    this.firstRender = false;
  },

  methods: {
    humanizeString,
    /**
     * Fetches all accepted SME datasets for the current company and converts them to the required frontend format.
     */
    async fetchNewEuTaxonomyData() {
      try {
        let fetchedData: DataAndMetaInformationNewEuTaxonomyDataForNonFinancials[];
        this.waitingForData = true;
        const newEuTaxonomyForNonFinancialsDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getNewEutaxonomyDataForNonFinancialsControllerApi();
        if (this.singleDataMetaInfoToDisplay) {
          const singleNewEuTaxonomyForNonFinancialsDataData = (
            await newEuTaxonomyForNonFinancialsDataControllerApi.getCompanyAssociatedNewEuTaxonomyDataForNonFinancials(
              this.singleDataMetaInfoToDisplay.dataId,
            )
          ).data.data;
          fetchedData = [
            { metaInfo: this.singleDataMetaInfoToDisplay, data: singleNewEuTaxonomyForNonFinancialsDataData },
          ];
        } else {
          fetchedData = (
            await newEuTaxonomyForNonFinancialsDataControllerApi.getAllCompanyNewEuTaxonomyDataForNonFinancials(
              assertDefined(this.companyId),
            )
          ).data;
        }
        this.convertedDataAndMetaInfo = fetchedData.map(
          (dataAndMetaInfo) => new DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel(dataAndMetaInfo),
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

    isFieldNameAmongEnvironmentalObjectives(fieldName: string): boolean {
      if (Object.values(EnvironmentalObjective).includes(fieldName)) {
      }
      return Object.values(EnvironmentalObjective).includes(fieldName);
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

    formatAmountWithCurrency(amountWithCurrency: AmountWithCurrency) {
      if (amountWithCurrency.amount == undefined) {
        return null;
      }
      return `${Math.round(amountWithCurrency.amount).toString()} ${amountWithCurrency.currency ?? ""}`;
    },

    formatPercentageNumber(relativeShareInPercent: number) {
      return `${relativeShareInPercent.toFixed(2).toString()} %`;
    },
  },
});
</script>
