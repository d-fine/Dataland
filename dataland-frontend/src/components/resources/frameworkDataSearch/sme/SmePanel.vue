<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.Sme) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData">
    <ThreeLayerTable
      :data-model="smeDataModel"
      :data-and-meta-info="smeDataAndMetaInfo"
      @data-converted="handleFinishedDataConversion"
      :format-value-for-display="formatValueForDisplay"
      :modal-column-headers="smeModalColumnHeaders"
    />
  </div>
</template>

<script lang="ts">
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import { smeDataModel } from "@/components/resources/frameworkDataSearch/sme/SmeDataModel";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { DataAndMetaInformationSmeData, DataTypeEnum, SmeProduct, SmeProductionSite } from "@clients/backend";
import Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import { humanizeString } from "@/utils/StringHumanizer";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { Field } from "@/utils/GenericFrameworkTypes";
import { smeModalColumnHeaders } from "@/components/resources/frameworkDataSearch/sme/SmeModalColumnHeaders";
import { convertToMillions } from "@/utils/NumberConversionUtils";

export default defineComponent({
  name: "SmePanel",
  components: { ThreeLayerTable },
  data() {
    return {
      DataTypeEnum,
      smeDataModel,
      firstRender: true,
      waitingForData: true,
      smeDataAndMetaInfo: [] as Array<DataAndMetaInformationSmeData>,
      smeModalColumnHeaders,
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
        this.waitingForData = true;
        const smeDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getSmeDataControllerApi();
        if (this.singleDataMetaInfoToDisplay) {
          const singleSmeData = (
            await smeDataControllerApi.getCompanyAssociatedSmeData(this.singleDataMetaInfoToDisplay.dataId)
          ).data.data;
          this.smeDataAndMetaInfo = [{ metaInfo: this.singleDataMetaInfoToDisplay, data: singleSmeData }];
        } else {
          this.smeDataAndMetaInfo = (
            await smeDataControllerApi.getAllCompanySmeData(assertDefined(this.companyId))
          ).data;
        }
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
      const fieldsToConvertToMillions = ["revenueInEur", "operatingCostInEur", "capitalAssetsInEur"];
      const optionFields = [
        "percentageOfInvestmentsInEnhancingEnergyEfficiency",
        "energyConsumptionCoveredByOwnRenewablePowerGeneration",
      ];
      if (value == null) {
        return value;
      } else if (field.name == "addressOfHeadquarters") {
        return this.formatAddress(value as object);
      } else if (optionFields.includes(field.name)) {
        return assertDefined(assertDefined(field.options).find((option) => option.value === value).label);
      } else if (field.name == "listOfProductionSites") {
        const listOfProductionSites = value as SmeProductionSite[];
        return listOfProductionSites.map((productionSite) => ({
          nameOfProductionSite: productionSite.nameOfProductionSite,
          addressOfProductionSite: productionSite.addressOfProductionSite,
          percentageOfTotalRevenue: productionSite.percentageOfTotalRevenue
            ? `${productionSite.percentageOfTotalRevenue}%`
            : undefined,
        }));
      } else if (field.name == "listOfProducts") {
        const listOfProducts = value as SmeProduct[];
        return listOfProducts.map((product) => ({
          name: product.name,
          percentageOfTotalRevenue: product.percentageOfTotalRevenue
            ? `${product.percentageOfTotalRevenue}%`
            : undefined,
        }));
      } else if (fieldsToConvertToMillions.includes(field.name)) {
        return convertToMillions(value as number);
      }
      return value;
    },
    /**
     * Formats an address to a multiline string
     * @param addressObject the address in object form
     * @returns the multiline address string
     */
    formatAddress(addressObject: object): string {
      /**
       * Tests if a provided string is defined and non-empty
       * @param value the string to test
       * @returns true if the string is defined and non-empty, else false
       */
      function isProperString(value?: string): boolean {
        return value != undefined && value != "";
      }

      /**
       * Tries to get the value of a field with a key or undefined if no such field was found
       * @param key the key to get the data for
       * @returns the value corresponding to the key or undefined if no such field was found
       */
      function getEntryValue(key: string): string | undefined {
        const searchResult = Object.entries(addressObject).find((entry) => entry[0] == key);
        return searchResult ? (searchResult[1] as string) : undefined;
      }

      let addressString = "";
      const streetAndHouseNumber = getEntryValue("streetAndHouseNumber");
      if (isProperString(streetAndHouseNumber)) {
        addressString += `${assertDefined(streetAndHouseNumber)}\n`;
      }
      const postalCode = getEntryValue("postalCode");
      if (isProperString(postalCode)) {
        addressString += `${assertDefined(postalCode)} `;
      }
      addressString += `${assertDefined(getEntryValue("city"))}\n`;
      const state = getEntryValue("state");
      if (isProperString(state)) {
        addressString += `${assertDefined(state)}, `;
      }
      addressString += assertDefined(getEntryValue("country"));
      return addressString;
    },
  },
});
</script>
