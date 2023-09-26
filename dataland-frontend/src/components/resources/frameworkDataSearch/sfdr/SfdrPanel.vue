<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.Sfdr) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="!waitingForData">
    <MultiLayerDataTable
      :datasets="mldtDatasets"
      :config="sfdrDisplayConfiguration"
      caption="Datasets of the SFDR Framework"
    />
  </div>
</template>

<script lang="ts">
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { type DataAndMetaInformationSfdrData, DataTypeEnum, type SfdrData } from "@clients/backend";
import type Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { type MLDTDataset } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { sortCompanyAssociatedDataByReportingPeriod } from "@/utils/DataTableDisplay";
import { convertDataModel } from "@/components/resources/dataTable/conversion/MultiLayerDataTableConfigurationConverter";
import MultiLayerDataTable from "@/components/resources/dataTable/MultiLayerDataTable.vue";
import { sfdrDataModel } from "@/components/resources/frameworkDataSearch/sfdr/SfdrDataModel";

export default defineComponent({
  name: "SfdrPanel",
  components: { MultiLayerDataTable },
  data() {
    return {
      DataTypeEnum,
      firstRender: true,
      waitingForData: true,
      sfdrDisplayConfiguration: convertDataModel(sfdrDataModel),
      sfdrDataAndMetaInfo: [] as Array<DataAndMetaInformationSfdrData>,
    };
  },
  computed: {
    mldtDatasets(): Array<MLDTDataset<SfdrData>> {
      const sortedDataAndMetaInformation = sortCompanyAssociatedDataByReportingPeriod(this.sfdrDataAndMetaInfo);
      return sortedDataAndMetaInformation.map((it) => ({
        headerLabel: it.metaInfo.reportingPeriod,
        dataset: it.data,
      }));
    },
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.fetchSfdrData().catch((error) => console.log(error));
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.fetchSfdrData().catch((error) => console.log(error));
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    this.fetchSfdrData().catch((error) => console.log(error));
    this.firstRender = false;
  },

  methods: {
    humanizeString: humanizeStringOrNumber,
    /**
     * Fetches all accepted SFDR datasets for the current company and converts them to the required frontend format.
     */
    async fetchSfdrData() {
      try {
        this.waitingForData = true;
        const sfdrDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getSfdrDataControllerApi();
        if (this.singleDataMetaInfoToDisplay) {
          const singleSfdrData = (
            await sfdrDataControllerApi.getCompanyAssociatedSfdrData(this.singleDataMetaInfoToDisplay.dataId)
          ).data.data;
          this.sfdrDataAndMetaInfo = [{ metaInfo: this.singleDataMetaInfoToDisplay, data: singleSfdrData }];
        } else {
          this.sfdrDataAndMetaInfo = (
            await sfdrDataControllerApi.getAllCompanySfdrData(assertDefined(this.companyId))
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
