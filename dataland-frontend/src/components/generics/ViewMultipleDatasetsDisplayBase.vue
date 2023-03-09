<template>
  <ViewFrameworkBase
    :companyID="companyId"
    :dataType="dataType"
    @updateActiveDataMetaInfoForChosenFramework="handleUpdateActiveDataMetaInfo"
    @updateAvailableReportingPeriodsForChosenFramework="handleUpdateAvailableReportingPeriods"
  >
    <template v-slot:content>
      <div v-if="isListOfDataIdsToDisplayFound">
        <DatasetStatusIndicator
          :displayed-dataset="singleDataMetaInfoToDisplay"
          :link-to-active-page="linkToActiveView"
        >
        </DatasetStatusIndicator>

        <div class="grid">
          <div class="col-12 text-left">
            <h2 class="mb-0" data-test="frameworkDataTableTitle">{{ humanizeString(dataType) }}</h2>
          </div>
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
import { AxiosError } from "axios";
import Keycloak from "keycloak-js";
import DatasetStatusIndicator from "@/components/resources/frameworkDataSearch/DatasetStatusIndicator.vue";

export default defineComponent({
  name: "ViewMultipleDatasetsDisplayBase",
  components: { DatasetStatusIndicator, SfdrPanel, LksgPanel, ViewFrameworkBase },
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
  computed: {
    linkToActiveView() {
      const activeDatasetAvailable =
        this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo &&
        this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.size > 0;

      if (this.companyId && this.dataType && activeDatasetAvailable)
        return `/companies/${this.companyId}/frameworks/${this.dataType}`;
      return undefined;
    },
  },
  watch: {
    dataId(newDataId: string) {
      console.log("dataID watcher in Multi-View component executed"); // TODO
      if (newDataId) {
        this.setFlagsToDataNotFoundState();
        void this.getMetaDataForDataId(newDataId);
      } else {
        if (!this.reportingPeriod) {
          this.setSingleDataMetaInfoToDisplay(null);
        }
      }
    },
    reportingPeriod(newReportingPeriod: string) {
      console.log("reportingPeriod watcher in Multi-View component executed"); // TODO
      if (newReportingPeriod) {
        const dataMetaInfoForNewlyChosenReportingPeriod =
          this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(newReportingPeriod);
        if (dataMetaInfoForNewlyChosenReportingPeriod) {
          this.getMetaDataForDataId(dataMetaInfoForNewlyChosenReportingPeriod.dataId).catch((err) =>
            console.log(
              "Retrieving meta data information for data ID " +
                dataMetaInfoForNewlyChosenReportingPeriod.dataId +
                " failed with error " +
                String(err)
            )
          );
        } else {
          this.isReportingPeriodInUrlInvalid = true;
        }
      } else {
        if (!this.dataId) {
          this.setSingleDataMetaInfoToDisplay(null);
        }
      }
    },
  },

  // TODO this component is partly similar to ViewSingleDatasetDisplayBase => therefore we should align the order of methods to make it easy to have an overview while working in both files

  methods: {
    /**
     * Method to set flags that indicate found data
     */
    setFlagsToDataFoundState() {
      this.isListOfDataIdsToDisplayFound = true;
      this.isDataIdInUrlInvalid = false;
      this.isReportingPeriodInUrlInvalid = false;
    },

    /**
     * Method to set flags that indicate that fetching data is in progress
     */
    setFlagsToDataNotFoundState() {
      this.isListOfDataIdsToDisplayFound = false;
      this.isDataIdInUrlInvalid = false;
      this.isReportingPeriodInUrlInvalid = false;
    },

    /**
     * Method to handle the button that switches to the active data set for the currently selected reporting period
     */
    handleClickOnSwitchToActiveDatasetForCurrentlyChosenReportingPeriodButton() {
      if (this.singleDataMetaInfoToDisplay) {
        const currentReportingPeriod = this.singleDataMetaInfoToDisplay.reportingPeriod;
        if (this.companyId != null && this.dataType != null) {
          this.$router
            .push(`/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${currentReportingPeriod}`)
            .catch((err) =>
              console.log(
                "Setting route for reporting period " + currentReportingPeriod + " failed with error " + String(err)
              )
            );
        }
      }
    },

    /**
     * Method to handle an invalid data ID that was passed in URL
     */
    handleInvalidDataIdPassedInUrl() {
      console.log("invalidDataIdPassedInUrl"); // TODO
      this.isDataIdInUrlInvalid = true;
      this.isListOfDataIdsToDisplayFound = false;
    },

    /**
     * Method to handle an invalid reporting period that was passed in URL
     */
    handleInvalidReportingPeriodPassedInUrl() {
      console.log("invalidReportingPeriodPassedInUrl"); // TODO
      this.isReportingPeriodInUrlInvalid = true;
      this.isListOfDataIdsToDisplayFound = false;
    },

    /**
     * Method to set a data meta information object as the only one to display
     *
     * @param dataMetaInfoToDisplay the data meta information to display
     */
    setSingleDataMetaInfoToDisplay(dataMetaInfoToDisplay: DataMetaInformation | null) {
      this.setFlagsToDataFoundState();
      this.singleDataMetaInfoToDisplay = dataMetaInfoToDisplay;
    },

    /**
     * Method to asynchronously retrieve the meta data associated to a given data ID
     *
     * @param dataId the data id to retrieve meta info for
     */
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
          this.setSingleDataMetaInfoToDisplay(dataMetaInfoForDataSetWithDataIdFromUrl);
        }
      } catch (error) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status == 404) {
          this.handleInvalidDataIdPassedInUrl();
        }
      }
    },

    /**
     * Method to asynchronously create a list of all data meta information objects for the displayed data sets
     */
    async createListOfDataMetaInfoForDisplayedDatasets() {
      if (this.dataId) {
        console.log("Case A for multiview"); // TODO debugging
        await this.getMetaDataForDataId(this.dataId);
      } else if (!this.dataId && this.reportingPeriod) {
        console.log("Case B for multiview"); // TODO debugging
        const activeDataMetaInfoWithReportingPeriodFromUrl =
          this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(this.reportingPeriod);
        if (activeDataMetaInfoWithReportingPeriodFromUrl) {
          this.setSingleDataMetaInfoToDisplay(activeDataMetaInfoWithReportingPeriodFromUrl);
        } else {
          this.handleInvalidReportingPeriodPassedInUrl();
        }
      } else {
        console.log("Case C for multiview"); // TODO debugging
        this.setFlagsToDataFoundState();
      }
    },

    /**
     * Method to handle the update of the available reporting periods
     *
     * @param receivedListOfAvailableReportingPeriods Desired new available reporting periods
     */
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
      console.log("handleUpdateActiveDataMetaInfo"); // TODO
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
