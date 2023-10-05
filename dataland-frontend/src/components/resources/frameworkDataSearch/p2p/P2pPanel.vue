<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.P2p) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData">
    <ThreeLayerTable
      :data-model="p2pDataModel"
      :data-and-meta-info="p2pDataAndMetaInfo.map((it) => getViewModelWithIdentityApiModel(it))"
      @data-converted="handleFinishedDataConversion"
      :format-value-for-display="formatValueForDisplay"
      :modal-column-headers="p2pModalColumnHeaders"
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
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import { getViewModelWithIdentityApiModel } from "@/components/resources/ViewModel";
import { formatValueForDisplay } from "@/components/resources/frameworkDataSearch/p2p/P2pFormatValueForDisplay";
import { p2pModalColumnHeaders } from "@/components/resources/frameworkDataSearch/p2p/P2pModalColumnHeaders";

export default defineComponent({
  name: "P2pPanel",

  components: { ThreeLayerTable },
  data() {
    return {
      DataTypeEnum,
      p2pModalColumnHeaders,
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
    formatValueForDisplay,
    getViewModelWithIdentityApiModel,
    humanizeString: humanizeStringOrNumber,
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
  },
});
</script>
