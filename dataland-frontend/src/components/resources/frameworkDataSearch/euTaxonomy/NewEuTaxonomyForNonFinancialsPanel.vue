<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.EutaxonomyNonFinancials) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData">
    <ThreeLayerTable
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

export default defineComponent({
  name: "EuTaxonomyForNonFinancialsPanel",
  components: { ThreeLayerTable },
  data() {
    return {
      DataTypeEnum,
      firstRender: true,
      waitingForData: true,
      convertedDataAndMetaInfo: [] as Array<DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel>,
      euTaxonomyForNonFinancialsModalColumnHeaders: newEuTaxonomyForNonFinancialsModalColumnHeaders,
      euTaxonomyForNonFinancialsDisplayDataModel: newEuTaxonomyForNonFinancialsDisplayDataModel,
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.fetchSmeData().catch((error) => console.log(error));
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.fetchSmeData().catch((error) => console.log(error));
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    this.fetchSmeData().catch((error) => console.log(error));
    this.firstRender = false;
  },

  methods: {
    humanizeString,
    /**
     * Fetches all accepted SME datasets for the current company and converts them to the required frontend format.
     */
    async fetchSmeData() {
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
     * Formats KPI values for display
     * @param field the considered KPI field
     * @param value the value to be formatted
     * @returns the formatted value
     */
    formatValueForDisplay(field: Field, value: KpiValue): KpiValue {
      if (value == null) {
        return value;
      } else if (field.name == "percentage") {
        return `${value} %`;
      } else if (field.name == "absoluteShare") {
        const amountWithCurrency = value as AmountWithCurrency;
        if (amountWithCurrency.amount == undefined) {
          return null;
        }
        return `${amountWithCurrency.amount.toString()}` + amountWithCurrency.currency
          ? ` ${amountWithCurrency.currency}`
          : "";
      }
      return value;
    },
  },
});
</script>
