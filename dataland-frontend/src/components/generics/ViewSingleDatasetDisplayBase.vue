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
        :placeholder="currentReportingPeriod?.length ? currentReportingPeriod : 'Select...'"
        aria-label="Choose reporting period"
        :class="[currentReportingPeriod?.length ? ['always-fill'] : '']"
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
      waitingForDataIdToDisplay: true,
      mapOfDistinctReportingPeriodsToActiveDataMetaInfo: {} as Map<string, DataMetaInformation>,
      reportingPeriodsInDropdown: [] as Array<string>,
      chosenReportingPeriodInDropdown: "",
      currentReportingPeriod: null as string | null,
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

  watch: {
    reportingPeriod(newValue) {
      // TODO broken         two times the same request. see console in browser!
      console.log("watcherRuns"); // TODO debugging
      console.log("new vlaue is " + newValue);  // TODO debugging
      if (newValue == null) {
        console.log("value switchedToNull");
      }
      if (Number.isNaN(this.reportingPeriod)) {
        console.log("reporting period is NaN"); // TODO debugging
      }
      if (!Number.isNaN(this.reportingPeriod)) {
        console.log("reporting period is " + this.reportingPeriod); // TODO debugging
        this.changeReportingPeriod(newValue);
      }
    },
  },

  methods: {
    changeRouteToReportingPeriod(reportingPeriod: string) {
      this.$router.push(`/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${reportingPeriod}`);
    },

    /**
     * Handles the change event of the reporting period dropdown to make the page display the active data set for the
     * newly selected reporting period.
     *
     * @param dropDownChangeEvent The object which is passed by the change event of the reporting period dropdown
     */
    handleChangeReportingPeriodEvent(dropDownChangeEvent: DropdownChangeEvent) {
      this.changeReportingPeriod(dropDownChangeEvent.value);
    },

    changeReportingPeriod(newReportingPeriod: string) {
      const dataMetaInfoForChosenReportingPeriod =
        this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(newReportingPeriod);
      if (dataMetaInfoForChosenReportingPeriod) {
        this.chosenReportingPeriodInDropdown = dataMetaInfoForChosenReportingPeriod.reportingPeriod;
        this.$emit("updateDataIdOfDatasetToDisplay", dataMetaInfoForChosenReportingPeriod.dataId);
      }
      this.changeRouteToReportingPeriod(newReportingPeriod);
    },

    /**
     * Switches to the active dataset for the currently chosen reporting period.
     */
    switchToActiveDatasetForCurrentlyChosenReportingPeriod() {
      const latestDataMetaInfoForCurrentReportingPeriod = this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(
        this.currentReportingPeriod as string
      );
      if (latestDataMetaInfoForCurrentReportingPeriod) {
        this.foundDataIdBelongsToOutdatedDataset = !latestDataMetaInfoForCurrentReportingPeriod.currentlyActive;
        this.$emit("updateDataIdOfDatasetToDisplay", latestDataMetaInfoForCurrentReportingPeriod.dataId);
        this.changeRouteToReportingPeriod(this.currentReportingPeriod as string);
      }
    },

    handleUpdateActiveDataMetaInfo(
      receivedMapOfReportingPeriodsToActiveDataMetaInfo: Map<string, DataMetaInformation>
    ) {
      this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo = receivedMapOfReportingPeriodsToActiveDataMetaInfo;
      this.reportingPeriodsInDropdown = Array.from(receivedMapOfReportingPeriodsToActiveDataMetaInfo.keys()).sort(
        (reportingPeriodA, reportingPeriodB) => {
          if (reportingPeriodA > reportingPeriodB) return -1;
          else return 0;
        }
      );
      this.chooseDataMetaInfoForDisplayedDataset();
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
        this.currentReportingPeriod = latestReportingPeriod;
        const activeDataMetaInfoForLatestReportingPeriod =
          this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(latestReportingPeriod);
        if (activeDataMetaInfoForLatestReportingPeriod) {
          return activeDataMetaInfoForLatestReportingPeriod;
        } else {
          return defaultActiveDataMetaInfo;
        }
      } else return defaultActiveDataMetaInfo;
    },

    /**
     * TODO adjust: Displays either the data set using the ID from the query param or if that is not available the first data set from the list of received data sets.
     */
    async chooseDataMetaInfoForDisplayedDataset() {
      if (this.dataId) {
        try {
          const metaDataControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
          ).getMetaDataControllerApi();
          const apiResponse = await metaDataControllerApi.getDataMetaInfo(this.dataId);
          const dataMetaInfoForDataSetWithDataIdFromUrl = apiResponse.data;
          this.currentReportingPeriod = dataMetaInfoForDataSetWithDataIdFromUrl.reportingPeriod;
          this.foundDataIdToDisplay = true;
          this.foundDataIdBelongsToOutdatedDataset = !dataMetaInfoForDataSetWithDataIdFromUrl.currentlyActive;
          this.$emit("updateDataIdOfDatasetToDisplay", this.dataId);
        } catch (error) {
          const axiosError = error as AxiosError;
          if (axiosError.response?.status == 404) {
            this.isDataIdInUrlValid = false;
          }
        }
      } else if (!this.dataId && this.reportingPeriod) {
        const activeDataMetaInfoWithReportingPeriodFromUrl = this.mapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(
          this.reportingPeriod
        );
        if (activeDataMetaInfoWithReportingPeriodFromUrl) {
          this.currentReportingPeriod = this.reportingPeriod;
          this.foundDataIdToDisplay = true;
          this.foundDataIdBelongsToOutdatedDataset = !activeDataMetaInfoWithReportingPeriodFromUrl.currentlyActive;
          this.$emit("updateDataIdOfDatasetToDisplay", activeDataMetaInfoWithReportingPeriodFromUrl.dataId);
        } else {
          this.isReportingPeriodInUrlValid = false;
        }
      } else {
        const dataMetaInfoForEmit = this.getActiveDataMetaInfoFromLatestReportingPeriodIfParsableAsNumber();
        if (dataMetaInfoForEmit) {
          this.foundDataIdToDisplay = true; // TODO think about this later again
          this.foundDataIdBelongsToOutdatedDataset = !dataMetaInfoForEmit.currentlyActive;
          this.$emit("updateDataIdOfDatasetToDisplay", dataMetaInfoForEmit.dataId);
          this.changeRouteToReportingPeriod(this.currentReportingPeriod as string);
        }
      }
    },
  },
});
</script>

// TODO back button is broken if you want to go back to the last period you viewed!
