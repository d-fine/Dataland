<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.P2p) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData">
    <MultiLayerDataTable :datasets="mldtDatasets" :config="p2pDisplayConfiguration" />
  </div>
</template>

<script lang="ts">
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import { p2pDataModel } from "@/components/resources/frameworkDataSearch/p2p/P2pDataModel";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import {
  type DataAndMetaInformationPathwaysToParisData,
  DataTypeEnum,
  type PathwaysToParisData,
} from "@clients/backend";
import type Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { type MLDTDataset } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { sortCompanyAssociatedDataByReportingPeriod } from "@/utils/DataTableDisplay";
import { convertDataModel } from "@/components/resources/dataTable/conversion/MultiLayerDataTableConfigurationConverter";
import MultiLayerDataTable from "@/components/resources/dataTable/MultiLayerDataTable.vue";

export default defineComponent({
  name: "P2pPanel",
  components: { MultiLayerDataTable },
  data() {
    return {
      DataTypeEnum,
      firstRender: true,
      waitingForData: true,
      p2pDisplayConfiguration: convertDataModel(p2pDataModel),
      p2pDataAndMetaInfo: [] as Array<DataAndMetaInformationPathwaysToParisData>,
    };
  },
  computed: {
    mldtDatasets(): Array<MLDTDataset<PathwaysToParisData>> {
      const sortedDataAndMetaInformation = sortCompanyAssociatedDataByReportingPeriod(this.p2pDataAndMetaInfo);
      return sortedDataAndMetaInformation.map((it) => ({
        headerLabel: it.metaInfo.reportingPeriod,
        dataset: it.data,
      }));
    },
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
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
