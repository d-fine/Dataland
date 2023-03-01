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
          placeholderForReportingPeriodDropdown?.length ? placeholderForReportingPeriodDropdown : 'Select...'
        "
        aria-label="Choose reporting period"
        :class="[placeholderForReportingPeriodDropdown?.length ? ['always-fill'] : '']"
        class="fill-dropdown ml-4"
        dropdownIcon="pi pi-angle-down"
        @change="handleChangeReportingPeriodEvent"
      />
    </template>

    <template v-slot:content>
      <div v-if="foundDataIdToDisplay">
        <div v-if="foundDataIdBelongsToOutdatedDataset" class="flex w-full info-bar">
          <span class="flex-1">this dataset is outdated</span>
          <PrimeButton
            @click="switchToActiveDatasetForCurrentlyChosenReportingPeriod"
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
      <div v-if="waitingForDataIdToDisplay" class="col-12 text-left">
        <h2>Checking if {{ dataDescriptor }} available...</h2>
      </div>
      <div
        v-if="!waitingForDataIdToDisplay && mapOfDistinctReportingPeriodsToActiveDataMetaInfo.length === 0"
        class="col-12 text-left"
      >
        <h2>No {{ dataDescriptor }} present</h2>
      </div>
      <div v-if="!isDataIdInUrlValid">
        <h2>No {{ dataDescriptor }} data could be found for the data ID {{ dataId }}.</h2>
      </div>
      <div v-if="!isReportingPeriodInUrlValid">
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
      waitingForDataIdToDisplay: true,
      mapOfDistinctReportingPeriodsToActiveDataMetaInfo: {} as Map<string, DataMetaInformation>,
      reportingPeriodsInReceivedDataMetaInfo: [] as Array<string>,
      reportingPeriodsInDropdown: [] as Array<string>,
      chosenReportingPeriodInDropdown: "",
      placeholderForReportingPeriodDropdown: null as string | null, // TODO at the very end: think about removing this. is it possible?
      isDataIdInUrlValid: true,
      isReportingPeriodInUrlValid: true,
      foundDataIdToDisplay: false,
      foundDataIdBelongsToOutdatedDataset: false,
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
      console.log("dataId watcher runs");
      if (newDataId) {
        console.log("dataId watcher does things");
        const dataMetaInfoForThisDataId = (await this.getMetaDataForDataIdAndReturnAndEmit(
          newDataId
        )) as DataMetaInformation;
        this.chosenReportingPeriodInDropdown = dataMetaInfoForThisDataId.reportingPeriod;
      }
    },
    reportingPeriod(newReportingPeriod) {
      "newReportingPeriod watcher runs";
      // this.placeholderForReportingPeriodDropdown = newReportingPeriod
      // this.filterReportingPeriodsInDropdown(newReportingPeriod)
      if (this.isReportingPeriodsWatcherEnabledForNextExecution && newReportingPeriod) {
        console.log("reportingPeriod watcher does things"); // TODO debug
        this.changeReportingPeriod(newReportingPeriod);
      } else {
        console.log("reportingPeriod watcher deactivated => does nothing");
        this.isReportingPeriodsWatcherEnabledForNextExecution = true;
      }
    },
  },

  methods: {
    filterReportingPeriodsInDropdown(reportinPeriodToFilterOut: string) {
      this.reportingPeriodsInDropdown = this.reportingPeriodsInDropdown.filter(
        (singleReportingPeriod) => reportinPeriodToFilterOut != singleReportingPeriod
      );
    },

    routerPushToReportingPeriod(reportingPeriod: string) {
      this.$router.push(`/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${reportingPeriod}`);
    },

    /**
     * Handles the change event of the reporting period dropdown to make the page display the active data set for the
     * newly selected reporting period.
     *
     * @param dropDownChangeEvent The object which is passed by the change event of the reporting period dropdown
     */
    handleChangeReportingPeriodEvent(dropDownChangeEvent: DropdownChangeEvent) {
      console.log("change event of reporting period dropdown");
      this.isReportingPeriodsWatcherEnabledForNextExecution = false;
      console.log("placeholderValue: " + this.placeholderForReportingPeriodDropdown);
      console.log("new reporting period from change event: " + dropDownChangeEvent.value);
      if (dropDownChangeEvent.value != this.placeholderForReportingPeriodDropdown) {
        this.changeReportingPeriod(dropDownChangeEvent.value);
      }
    },

    changeReportingPeriod(newReportingPeriod: string) {
      if (!this.isReportingPeriodInUrlValid) {
        this.isReportingPeriodInUrlValid = true;
      }
      const dataMetaInfoForChosenReportingPeriod =
        this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(newReportingPeriod);
      if (dataMetaInfoForChosenReportingPeriod) {
        this.chosenReportingPeriodInDropdown = dataMetaInfoForChosenReportingPeriod.reportingPeriod;
        this.foundDataIdToDisplay = true;
        this.foundDataIdBelongsToOutdatedDataset = !dataMetaInfoForChosenReportingPeriod.currentlyActive;
        this.$emit("updateDataIdOfDatasetToDisplay", dataMetaInfoForChosenReportingPeriod.dataId);
      }
      this.routerPushToReportingPeriod(newReportingPeriod);
      this.placeholderForReportingPeriodDropdown = newReportingPeriod;
    },

    /**
     * Switches to the active dataset for the currently chosen reporting period.
     */
    switchToActiveDatasetForCurrentlyChosenReportingPeriod() {
      // TODO check if still needed or if you can use other method and remove this one
      this.isReportingPeriodsWatcherEnabledForNextExecution = false;
      const latestDataMetaInfoForCurrentReportingPeriod = this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(
        this.placeholderForReportingPeriodDropdown as string
      );
      if (latestDataMetaInfoForCurrentReportingPeriod) {
        this.foundDataIdBelongsToOutdatedDataset = !latestDataMetaInfoForCurrentReportingPeriod.currentlyActive;
        this.$emit("updateDataIdOfDatasetToDisplay", latestDataMetaInfoForCurrentReportingPeriod.dataId);
        this.routerPushToReportingPeriod(this.placeholderForReportingPeriodDropdown as string);
      }
    },

    handleUpdateActiveDataMetaInfo(
      receivedMapOfReportingPeriodsToActiveDataMetaInfo: Map<string, DataMetaInformation>
    ) {
      this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo = receivedMapOfReportingPeriodsToActiveDataMetaInfo;
      // TODO
      this.reportingPeriodsInDropdown = Array.from(receivedMapOfReportingPeriodsToActiveDataMetaInfo.keys()).sort(
        (reportingPeriodA, reportingPeriodB) => {
          if (reportingPeriodA > reportingPeriodB) return -1;
          else return 0;
        }
      );
      this.chooseDataMetaInfoForDisplayedDatasetAndEmitDataId();
      this.waitingForDataIdToDisplay = false;
    },

    getActiveDataMetaInfoFromLatestReportingPeriodIfParsableAsNumber(): DataMetaInformation {
      const [firstActiveDataMetaInfo] = this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.values();
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
        this.placeholderForReportingPeriodDropdown = latestReportingPeriod;
        const activeDataMetaInfoForLatestReportingPeriod =
          this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(latestReportingPeriod);
        if (activeDataMetaInfoForLatestReportingPeriod) {
          return activeDataMetaInfoForLatestReportingPeriod;
        } else {
          return defaultActiveDataMetaInfo;
        }
      } else return defaultActiveDataMetaInfo;
    },

    async getMetaDataForDataIdAndReturnAndEmit(dataId: string) {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getDataMetaInfo(dataId);
        const dataMetaInfoForDataSetWithDataIdFromUrl = apiResponse.data;
        this.placeholderForReportingPeriodDropdown = dataMetaInfoForDataSetWithDataIdFromUrl.reportingPeriod;
        this.foundDataIdToDisplay = true;
        this.foundDataIdBelongsToOutdatedDataset = !dataMetaInfoForDataSetWithDataIdFromUrl.currentlyActive;
        this.$emit("updateDataIdOfDatasetToDisplay", this.dataId);
        return dataMetaInfoForDataSetWithDataIdFromUrl;
      } catch (error) {
        const axiosError = error as AxiosError;
        if (axiosError.response?.status == 404) {
          this.isDataIdInUrlValid = false;
        }
      }
    },

    switchToActiveDatasetForReportingPeriodInUrl() {
      if (this.reportingPeriod) {
        const activeDataMetaInfoWithReportingPeriodFromUrl = this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(
          this.reportingPeriod
        );
        if (activeDataMetaInfoWithReportingPeriodFromUrl) {
          this.placeholderForReportingPeriodDropdown = this.reportingPeriod;
          this.foundDataIdToDisplay = true;
          this.foundDataIdBelongsToOutdatedDataset = !activeDataMetaInfoWithReportingPeriodFromUrl.currentlyActive;
          this.$emit("updateDataIdOfDatasetToDisplay", activeDataMetaInfoWithReportingPeriodFromUrl.dataId);
        } else {
          this.isReportingPeriodInUrlValid = false;
        }
      }
    },

    switchToDefaultDatasetToDisplay() {
      const dataMetaInfoForEmit = this.getActiveDataMetaInfoFromLatestReportingPeriodIfParsableAsNumber();
      if (dataMetaInfoForEmit) {
        this.foundDataIdToDisplay = true;
        this.foundDataIdBelongsToOutdatedDataset = !dataMetaInfoForEmit.currentlyActive;
        this.$emit("updateDataIdOfDatasetToDisplay", dataMetaInfoForEmit.dataId);
        this.isReportingPeriodsWatcherEnabledForNextExecution = false;
        this.$router.replace(
          `/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${
            this.placeholderForReportingPeriodDropdown as string
          }`
        );
        // this.filterReportingPeriodsInDropdown(dataMetaInfoForEmit.reportingPeriod)
      }
    },

    /**
     * TODO adjust: Displays either the data set using the ID from the query param or if that is not available the first data set from the list of received data sets.
     */
    async chooseDataMetaInfoForDisplayedDatasetAndEmitDataId() {
      if (this.dataId) {
        console.log("A"); // TODO debugging
        await this.getMetaDataForDataIdAndReturnAndEmit(this.dataId);
      } else if (!this.dataId && this.reportingPeriod) {
        console.log("B"); // TODO debugging
        this.switchToActiveDatasetForReportingPeriodInUrl();
      } else {
        console.log("C"); // TODO debugging
        this.switchToDefaultDatasetToDisplay();
      }
    },
  },
});
</script>

// TODO back button is broken if you want to go back to the last period you viewed!
