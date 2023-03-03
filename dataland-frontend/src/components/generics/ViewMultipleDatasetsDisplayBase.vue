<template>
  <ViewFrameworkBase
    :companyID="companyId"
    :dataType="dataType"
    @updateActiveDataMetaInfoForChosenFramework="handleUpdateActiveDataMetaInfo"
    @updateAvailableReportingPeriodsForChosenFramework="handleUpdateAvailableReportingPeriods"
  >
    <template v-slot:content>
      <div v-if="isListOfDataIdsToDisplayFound">
        <div class="grid">
          <div class="col-12">
            <LksgPanel
              v-if="dataType === DataTypeEnum.Lksg"
              :companyId="companyId"
              :singleDataMetaInfoToDisplay="singleDataMetaInfoToDisplay"
            />
            <SfdrPanel
              v-if="dataType === DataTypeEnum.Sfdr"
              :companyId="companyId"
              :singleDataMetaInfoToDisplay="singleDataMetaInfoToDisplay"
            />
          </div>
        </div>
      </div>
      <div v-if="isWaitingForListOfDataIdsToDisplay" class="col-12 text-left">
        <h2>Checking if {{ humanizeString(dataType) }} data available...</h2>
      </div>
      <div
        v-if="
          !isWaitingForListOfDataIdsToDisplay && receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.size === 0
        "
        class="col-12 text-left"
      >
        <h2>No {{ humanizedDataDescription }} data present for this company.</h2>
      </div>
      <div v-if="isDataIdInUrlInvalid">
        <h2>
          No {{ humanizedDataDescription }} data could be found for the data ID passed in the URL for this company.
        </h2>
      </div>
      <div v-if="isReportingPeriodInUrlInvalid">
        <h2>
          No {{ humanizedDataDescription }} data could be found for the reporting period passed in the URL for this
          company.
        </h2>
      </div>
    </template>
  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import { defineComponent, inject } from "vue";
import { DataMetaInformation } from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";
import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import { DataTypeEnum } from "@clients/backend";
import SfdrPanel from "@/components/resources/frameworkDataSearch/sfdr/SfdrPanel.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { AxiosError } from "axios/index";
import Keycloak from "keycloak-js";

export default defineComponent({
  name: "ViewMultipleDatasetsDisplayBase",
  components: { SfdrPanel, LksgPanel, ViewFrameworkBase },
  props: {
    companyId: {
      type: String,
    },
    dataType: {
      type: String,
    },
    dataId: {
      type: String,
    },
    reportingPeriod: {
      type: String,
    },
  },
  data() {
    return {
      isWaitingForListOfDataIdsToDisplay: true,
      isListOfDataIdsToDisplayFound: false,
      receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo: {} as Map<string, DataMetaInformation>,
      singleDataMetaInfoToDisplay: null as null | DataMetaInformation,
      distinctAvailableReportingPeriods: [] as string[], // TODO note:    this is needed for the popup when you click "EDIT"
      humanizeString: humanizeString,
      isDataIdInUrlInvalid: false,
      isReportingPeriodInUrlInvalid: false,
      humanizedDataDescription: humanizeString(this.dataType),
      DataTypeEnum,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  methods: {
    handleInvalidDataIdPassedInUrl() {
      // TODO wip
      this.isDataIdInUrlInvalid = true;
      this.isListOfDataIdsToDisplayFound = false;
    },

    handleInvalidReportingPeriodPassedInUrl() {
      // TODO wip
      this.isReportingPeriodInUrlInvalid = true;
    },

    async getMetaDataForDataId(dataId: string) {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getDataMetaInfo(dataId);
        const dataMetaInfoForDataSetWithDataIdFromUrl = apiResponse.data;
        if (dataMetaInfoForDataSetWithDataIdFromUrl.companyId != this.companyId) {
          this.handleInvalidDataIdPassedInUrl();
        } else {
          this.isListOfDataIdsToDisplayFound = true;
          this.singleDataMetaInfoToDisplay = dataMetaInfoForDataSetWithDataIdFromUrl;
        }
      } catch (error) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status == 404) {
          this.handleInvalidDataIdPassedInUrl();
        }
      }
    },

    async createListOfDataMetaInfoForDisplayedDatasets() {
      if (this.dataId) {
        console.log("Case A for multiview"); // TODO debugging
        await this.getMetaDataForDataId(this.dataId);
      } else if (!this.dataId && this.reportingPeriod) {
        console.log("Case B for multiview"); // TODO debugging
        const activeDataMetaInfoWithReportingPeriodFromUrl =
          this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(this.reportingPeriod);
        if (activeDataMetaInfoWithReportingPeriodFromUrl) {
          this.isListOfDataIdsToDisplayFound = true;
        } else {
          this.handleInvalidReportingPeriodPassedInUrl();
        }
      } else {
        console.log("Case C for multiview"); // TODO debugging
        this.isListOfDataIdsToDisplayFound = true;
      }
    },

    handleUpdateAvailableReportingPeriods(receivedListOfAvailableReportingPeriods: string[]) {
      this.distinctAvailableReportingPeriods = receivedListOfAvailableReportingPeriods;
    },

    /**
     * TODO Stores the received data IDs from the "updateDataId" event and terminates the loading-state of the component.
     *
     * @param receivedMapOfReportingPeriodsToActiveDataMetaInfo 1-to-1 map between reporting periods and corresponding
     * active data meta information objects
     */
    handleUpdateActiveDataMetaInfo(
      receivedMapOfReportingPeriodsToActiveDataMetaInfo: Map<string, DataMetaInformation>
    ) {
      console.log(receivedMapOfReportingPeriodsToActiveDataMetaInfo);
      this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo =
        receivedMapOfReportingPeriodsToActiveDataMetaInfo;
      this.createListOfDataMetaInfoForDisplayedDatasets().catch((err) =>
        console.log("Retrieving data meta info failed with error " + String(err))
      );
      // TODO can we remove the sorting logic for same-year datasets from the lksg-view-page now?  because we receive only one per year now (because of "latest" setting)
      this.isWaitingForListOfDataIdsToDisplay = false;
    },
  },
});
</script>
