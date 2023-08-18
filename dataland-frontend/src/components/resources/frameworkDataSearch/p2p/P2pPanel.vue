<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.P2p) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData">
    <ThreeLayerTable
      :data-model="p2pDataModel"
      :data-and-meta-info="p2pDataAndMetaInfo"
      @data-converted="handleFinishedDataConversion"
      :format-value-for-display="formatValueForDisplay"
    />
  </div>
</template>

<script lang="ts">
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import { p2pDataModel } from "@/components/resources/frameworkDataSearch/p2p/P2pDataModel";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { type DataAndMetaInformationPathwaysToParisData, DataTypeEnum } from "@clients/backend";
import type Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import { humanizeString } from "@/utils/StringHumanizer";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { type KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";

export default defineComponent({
  name: "P2pPanel",

  components: { ThreeLayerTable },
  data() {
    return {
      DataTypeEnum,
      firstRender: true,
      waitingForData: true,
      p2pDataAndMetaInfo: [] as Array<DataAndMetaInformationPathwaysToParisData>,
      p2pDataModel,
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.fetchP2pData().catch((error) => console.log(error));
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
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
    humanizeString,
    /**
     * Fetches all accepted P2P datasets for the current company and converts them to the required frontend format.
     */
    async fetchP2pData() {
      try {
        this.waitingForData = true;
        const p2pDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
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
      if (field.name == "sector") {
        return (value as string[]).map((sector) => humanizeString(sector));
      }
      return value;
    },
  },
});
</script>
