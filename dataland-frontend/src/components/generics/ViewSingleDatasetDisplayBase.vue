<template>
  <ViewFrameworkBase
    :companyID="companyId"
    :dataType="dataType"
    @updateActiveDataMetaInfoForChosenFramework="handleUpdateActiveDataMetaInfo"
  >
    <template v-slot:reportingPeriodDropdown>
      <Dropdown
        id="chooseReportingPeriodDropdown"
        v-model="chosenReportingPeriodInDropdown"
        :options="reportingPeriodsInDropdown"
        :placeholder="
          latestChosenReportingPeriodInDropdown?.length ? latestChosenReportingPeriodInDropdown : 'Select...'
        "
        aria-label="Choose reporting period"
        :class="[latestChosenReportingPeriodInDropdown?.length ? ['always-fill'] : '']"
        class="fill-dropdown ml-4"
        dropdownIcon="pi pi-angle-down"
        @change="handleChangeReportingPeriodEvent"
      />
    </template>

    <template v-slot:content>
      <div v-if="isDataIdToDisplayFound">
        <div v-if="isFoundDataIdBelongingToOutdatedDataset" class="flex w-full info-bar">
          <span class="flex-1">this dataset is outdated</span>
          <PrimeButton
            @click="handleClickOnSwitchToActiveDatasetForCurrentlyChosenReportingPeriodButton"
            label="See latest version"
            icon="pi pi-stopwatch"
          />
        </div>
        <div class="grid">
          <div class="col-12 text-left">
            <h2 class="mb-0">{{ title }}</h2>
          </div>
          <div class="col-6 text-left">
            <p class="font-semibold text-gray-800 mt-0">Data from company report.</p>
          </div>
        </div>
        <div class="grid">
          <div class="col-7">
            <slot></slot>
          </div>
        </div>
      </div>
      <div v-if="isWaitingForDataIdToDisplay" class="col-12 text-left">
        <h2>Checking if {{ dataDescriptor }} available...</h2>
      </div>
      <div
        v-if="!isWaitingForDataIdToDisplay && receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.length === 0"
        class="col-12 text-left"
      >
        <h2>No {{ dataDescriptor }} present</h2>
      </div>
      <div v-if="isDataIdInUrlInvalid">
        <h2>No {{ dataDescriptor }} data could be found for the data ID {{ dataId }}.</h2>
      </div>
      <div v-if="isReportingPeriodInUrlInvalid">
        <h2>No {{ dataDescriptor }} data could be found for the reporting period {{ reportingPeriod }}.</h2>
      </div>
    </template>
  </ViewFrameworkBase>
</template>

<script lang="ts">
import ViewFrameworkBase from "@/components/generics/ViewFrameworkBase.vue";
import { DataMetaInformation, DataTypeEnum } from "@clients/backend";
import { defineComponent, inject, ref } from "vue";
import Dropdown, { DropdownChangeEvent } from "primevue/dropdown";
import Keycloak from "keycloak-js";
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { AxiosError } from "axios";
import PrimeButton from "primevue/button";

export default defineComponent({
  name: "ViewSingleDatasetDisplayBase",
  components: { ViewFrameworkBase, Dropdown, PrimeButton },
  props: {
    companyId: {
      type: String,
    },
    dataId: {
      type: String,
    },
    reportingPeriod: {
      type: String,
    },
    dataType: {
      type: String,
    },
    dataDescriptor: {
      type: String,
    },
    title: {
      type: String,
    },
  },

  emits: ["updateDataIdOfDatasetToDisplay"],

  data() {
    return {
      isReportingPeriodsWatcherEnabledForNextExecution: true,
      isWaitingForDataIdToDisplay: true,
      receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo: {} as Map<string, DataMetaInformation>,
      reportingPeriodsInReceivedDataMetaInfo: [] as Array<string>,
      reportingPeriodsInDropdown: [] as Array<string>,
      chosenReportingPeriodInDropdown: "",
      latestChosenReportingPeriodInDropdown: null as string | null,
      isDataIdInUrlInvalid: false,
      isReportingPeriodInUrlInvalid: false,
      isDataIdToDisplayFound: false,
      isFoundDataIdBelongingToOutdatedDataset: false,
    };
  },

  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      frameworkDataSearchBar: ref<typeof FrameworkDataSearchBar>(),
    };
  },

  mounted() {
    console.log("mounted"); // TODO debugging
  },

  watch: {
    async dataId(newDataId) {
      console.log("dataId watcher runs"); // TODO debugging
      if (newDataId) {
        console.log("dataId watcher does things"); // TODO debugging
        await this.getMetaDataForDataIdAndEmit(newDataId);
      }
    },
    reportingPeriod(newReportingPeriod) {
      "newReportingPeriod watcher runs";
      if (this.isReportingPeriodsWatcherEnabledForNextExecution && newReportingPeriod) {
        console.log("reportingPeriod watcher does things"); // TODO debug
        this.switchToActiveDatasetForNewlyChosenReportingPeriod(newReportingPeriod);
      } else {
        console.log("reportingPeriod watcher deactivated => does nothing");
      }
      this.isReportingPeriodsWatcherEnabledForNextExecution = true;
    },
  },

  methods: {
    processDataMetaInfoForDisplay(dataMetaInfoForDisplay: DataMetaInformation) {
      this.chosenReportingPeriodInDropdown = dataMetaInfoForDisplay.reportingPeriod;
      this.latestChosenReportingPeriodInDropdown = dataMetaInfoForDisplay.reportingPeriod;
      this.isDataIdToDisplayFound = true;
      this.isFoundDataIdBelongingToOutdatedDataset = !dataMetaInfoForDisplay.currentlyActive;
      this.$emit("updateDataIdOfDatasetToDisplay", dataMetaInfoForDisplay.dataId);
    },

    /**
     * Handles the change event of the reporting period dropdown to make the page display the active data set for the
     * newly selected reporting period.
     *
     * @param dropDownChangeEvent The object which is passed by the change event of the reporting period dropdown
     */
    handleChangeReportingPeriodEvent(dropDownChangeEvent: DropdownChangeEvent) {
      console.log("change event of reporting period dropdown");
      console.log("placeholderValue: " + this.latestChosenReportingPeriodInDropdown);
      console.log("new reporting period from change event: " + dropDownChangeEvent.value);
      if (dropDownChangeEvent.value != this.latestChosenReportingPeriodInDropdown) {
        this.switchToActiveDatasetForNewlyChosenReportingPeriod(dropDownChangeEvent.value);
      }
    },

    switchToActiveDatasetForNewlyChosenReportingPeriod(newReportingPeriod: string) {
      this.isReportingPeriodsWatcherEnabledForNextExecution = false;
      if (this.isReportingPeriodInUrlInvalid) this.isReportingPeriodInUrlInvalid = false;
      if (this.isDataIdInUrlInvalid) this.isDataIdInUrlInvalid = false;
      const dataMetaInfoForNewlyChosenReportingPeriod =
        this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(newReportingPeriod);
      if (dataMetaInfoForNewlyChosenReportingPeriod) {
        this.processDataMetaInfoForDisplay(dataMetaInfoForNewlyChosenReportingPeriod);
        this.routerPushToReportingPeriod(dataMetaInfoForNewlyChosenReportingPeriod.reportingPeriod);
      }
      else { this.isReportingPeriodInUrlInvalid = true }
    },

    routerPushToReportingPeriod(reportingPeriod: string) {
      this.$router.push(`/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${reportingPeriod}`);
    },

    /**
     * Switches to the active dataset for the currently chosen reporting period.
     */
    handleClickOnSwitchToActiveDatasetForCurrentlyChosenReportingPeriodButton() {
      this.switchToActiveDatasetForNewlyChosenReportingPeriod(this.latestChosenReportingPeriodInDropdown as string);
    },

    handleUpdateActiveDataMetaInfo(
      receivedMapOfReportingPeriodsToActiveDataMetaInfo: Map<string, DataMetaInformation>
    ) {
      this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo =
        receivedMapOfReportingPeriodsToActiveDataMetaInfo;
      this.getDistinctAvailableReportingPeriodsAndPutThemSortedIntoDropdown(
        receivedMapOfReportingPeriodsToActiveDataMetaInfo
      );
      this.chooseDataMetaInfoForDisplayedDatasetAndEmitDataId();
      this.isWaitingForDataIdToDisplay = false;
    },

    getDistinctAvailableReportingPeriodsAndPutThemSortedIntoDropdown(
      receivedMapOfReportingPeriodsToActiveDataMetaInfo: Map<string, DataMetaInformation>
    ) {
      this.reportingPeriodsInDropdown = Array.from(receivedMapOfReportingPeriodsToActiveDataMetaInfo.keys()).sort(
        (reportingPeriodA, reportingPeriodB) => {
          if (reportingPeriodA > reportingPeriodB) return -1;
          else return 0;
        }
      );
    },

    /**
     * TODO adjust: Displays either the data set using the ID from the query param or if that is not available the first data set from the list of received data sets.
     */
    async chooseDataMetaInfoForDisplayedDatasetAndEmitDataId() {
      if (this.dataId) {
        console.log("Case A"); // TODO debugging
        await this.getMetaDataForDataIdAndEmit(this.dataId);
      } else if (!this.dataId && this.reportingPeriod) {
        console.log("Case B"); // TODO debugging
        this.switchToActiveDatasetForNewlyChosenReportingPeriod(this.reportingPeriod);
      } else {
        console.log("Case C"); // TODO debugging
        this.switchToDefaultDatasetToDisplay();
      }
    },

    async getMetaDataForDataIdAndEmit(dataId: string) {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getDataMetaInfo(dataId);
        const dataMetaInfoForDataSetWithDataIdFromUrl = apiResponse.data;
        this.processDataMetaInfoForDisplay(dataMetaInfoForDataSetWithDataIdFromUrl);
      } catch (error) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status == 404) {
          // TODO we need to do smth here to set the reportingPeriod dropdown to "Select ...";  Reason: If you click "BACK" and land on the "invalid data ID" page, the reporting Period dropdown needs to adjust and display the "Select..." state
          this.latestChosenReportingPeriodInDropdown = null; // TODO this alone isn't working!
          this.isDataIdToDisplayFound = false;
          this.isDataIdInUrlInvalid = true;
        }
      }
    },

    switchToDefaultDatasetToDisplay() {
      const dataMetaInfoForEmit = this.getActiveDataMetaInfoFromLatestReportingPeriodIfParsableAsNumber();
      if (dataMetaInfoForEmit) {
        this.processDataMetaInfoForDisplay(dataMetaInfoForEmit);
        this.isReportingPeriodsWatcherEnabledForNextExecution = false;
        this.$router.replace(
          `/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${
            this.latestChosenReportingPeriodInDropdown as string
          }`
        );
      }
    },

    getActiveDataMetaInfoFromLatestReportingPeriodIfParsableAsNumber(): DataMetaInformation {
      const [firstActiveDataMetaInfo] = this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.values();
      const defaultActiveDataMetaInfo = [firstActiveDataMetaInfo][0];
      const listOfNumbersInReportingPeriods: number[] = [];
      this.reportingPeriodsInDropdown.forEach((reportingPeriodAsString) => {
        const parsedReportingPeriod = parseInt(reportingPeriodAsString);
        if (!Number.isNaN(parsedReportingPeriod)) {
          listOfNumbersInReportingPeriods.push(parsedReportingPeriod);
        }
      });
      if (listOfNumbersInReportingPeriods.length > 0) {
        const latestReportingPeriod = listOfNumbersInReportingPeriods.reduce((a, b) => Math.max(a, b)).toString();
        const activeDataMetaInfoForLatestReportingPeriod =
          this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(latestReportingPeriod);
        if (activeDataMetaInfoForLatestReportingPeriod) {
          return activeDataMetaInfoForLatestReportingPeriod;
        } else {
          return defaultActiveDataMetaInfo;
        }
      } else return defaultActiveDataMetaInfo;
    },
  },
});
</script>
