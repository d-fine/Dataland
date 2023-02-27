<template>
  <ViewFrameworkBase
    :companyID="companyId"
    :dataType="dataType"
    @updateAvailableReportingPeriodsForChosenFramework="handleUpdateAvailableReportingPeriods"
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
        @change="handleChangeReportingPeriod"
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
      <div v-if="waitingForDataMetaInfoAndChoosingDatasetToDisplay" class="col-12 text-left">
        <h2>Checking if {{ dataDescriptor }} available...</h2>
      </div>
      <div
        v-if="!waitingForDataMetaInfoAndChoosingDatasetToDisplay && listOfReceivedActiveDataMetaInfo.length === 0"
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
import { useRoute } from "vue-router";
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
      waitingForDataMetaInfoAndChoosingDatasetToDisplay: true,
      listOfReceivedActiveDataMetaInfo: [] as DataMetaInformation[],
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

  methods: {
    /**
     * Handles the change event of the reporting period dropdown to make the page display the active data set for the
     * newly selected reporting period.
     *
     * @param dropDownChangeEvent The object which is passed by the change event of the reporting period dropdown
     */
    handleChangeReportingPeriod(dropDownChangeEvent: DropdownChangeEvent) {
        const newlySelectedReportingPeriod = dropDownChangeEvent.value as string
        const listOfDataMetaInfoForChosenReportingPeriod = this.listOfReceivedActiveDataMetaInfo.filter(
            (dataMetaInfo) => dataMetaInfo.reportingPeriod == newlySelectedReportingPeriod
        );
        this.$emit(
            "updateDataIdOfDatasetToDisplay",
            this.getActiveDataMetaInfoFromListOfDataMetaInfoForSingleReportingPeriod(
                listOfDataMetaInfoForChosenReportingPeriod
            ).dataId
        );
        this.$router.push(
            `/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${newlySelectedReportingPeriod}`
        );
      },

    /**
     * Switches to the
     *
     * @param dropDownChangeEvent The object which is passed by the change event of the reporting period dropdown
     */
    switchToActiveDatasetForCurrentlyChosenReportingPeriod() {
      const latestDataMetaInfoForCurrentReportingPeriod = this.listOfReceivedActiveDataMetaInfo.filter(
        (dataMetaInfo) => dataMetaInfo.reportingPeriod == this.currentReportingPeriod
      )[0]; // TODO list needs to have 1 element! check??? necessary???
      this.foundDataIdBelongsToOutdatedDataset = !latestDataMetaInfoForCurrentReportingPeriod.currentlyActive;
      this.$emit("updateDataIdOfDatasetToDisplay", latestDataMetaInfoForCurrentReportingPeriod.dataId);
      this.$router.push(
        `/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${this.currentReportingPeriod}`
      );
    },

    getActiveDataMetaInfoFromListOfDataMetaInfoForSingleReportingPeriod(
      listOfDataMetaInfoForSingleReportingPeriod: DataMetaInformation[]
    ): DataMetaInformation {
      const activeDataMetaInfoForSingleReportingPeriod = listOfDataMetaInfoForSingleReportingPeriod.filter(
        (dataMetaInfo) => dataMetaInfo.currentlyActive
      );
      if (activeDataMetaInfoForSingleReportingPeriod.length == 1) {
        return activeDataMetaInfoForSingleReportingPeriod[0];
      } else if (activeDataMetaInfoForSingleReportingPeriod.length == 0) {
        throw TypeError(`The fetched data meta info for the current company ID ${this.companyId}
        and framework ${this.dataType} is empty.`);
      } else {
        throw TypeError(`The fetched data meta info for the current company ID ${this.companyId}
        and framework ${this.dataType} has several data meta info datsets with the status 'active'.
        Therefore no unique data ID could be determined.`);
      } // TODO error messages are a little misleading.   the list could also be broken because of invalid input param!
      // TODO that means that a list was passed, which has no elements, or more than one element with currentlyActive=true!  rewrite!
    },

    handleUpdateAvailableReportingPeriods(listOfAvailableReportingPeriods: string[]) {
      this.reportingPeriodsInDropdown = listOfAvailableReportingPeriods;
    },

    handleUpdateActiveDataMetaInfo(listOfReceivedDataMetaInfo: Array<DataMetaInformation>) {
      this.listOfReceivedActiveDataMetaInfo = listOfReceivedDataMetaInfo;
      this.chooseDataMetaInfoForDisplayedDataset();
      this.waitingForDataMetaInfoAndChoosingDatasetToDisplay = false;
    },

    getActiveDataMetaInfoFromLatestReportingPeriodIfNumberFound(): DataMetaInformation {
      const numbersInReportingPeriodsAsStrings = this.reportingPeriodsInDropdown.filter(
        (reportingPeriod) => !isNaN(parseInt(reportingPeriod))
      );
      if (numbersInReportingPeriodsAsStrings.length > 0) {
        const integersInReportingPeriods = numbersInReportingPeriodsAsStrings.map((integerAsString) =>
          parseInt(integerAsString)
        );
        const latestReportingPeriod = integersInReportingPeriods.reduce((a, b) => Math.max(a, b)).toString();
        this.currentReportingPeriod = latestReportingPeriod;
        // assure that active TODO  else throw an error
        return this.getActiveDataMetaInfoFromListOfDataMetaInfoForSingleReportingPeriod(
          this.listOfReceivedActiveDataMetaInfo.filter(
            (dataMetaInfo: DataMetaInformation) => dataMetaInfo.reportingPeriod == latestReportingPeriod
          )
        )
      } else {
        return this.listOfReceivedActiveDataMetaInfo[0]
      }
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
        const listOfDataMetaInfoWithReportingPeriodFromUrl = this.listOfReceivedActiveDataMetaInfo.filter(
          (dataMetaInfo) => dataMetaInfo.reportingPeriod === this.reportingPeriod
        );
        if (listOfDataMetaInfoWithReportingPeriodFromUrl.length == 1) {
          const dataMetaInfoToEmit = listOfDataMetaInfoWithReportingPeriodFromUrl[0];
          this.currentReportingPeriod = this.reportingPeriod;
          this.foundDataIdToDisplay = true;
          this.foundDataIdBelongsToOutdatedDataset = !dataMetaInfoToEmit.currentlyActive;
          this.$emit("updateDataIdOfDatasetToDisplay", dataMetaInfoToEmit.dataId);
        } else {
          this.isReportingPeriodInUrlValid = false;
        }
      } else {
        const dataMetaInfoForEmit = this.getActiveDataMetaInfoFromLatestReportingPeriodIfNumberFound();
        if (dataMetaInfoForEmit) {
          this.foundDataIdToDisplay = true; // TODO think about this later again
          this.foundDataIdBelongsToOutdatedDataset = !dataMetaInfoForEmit.currentlyActive;
          this.$emit(
            // TODO duplicate code block more or less => put in own function
            "updateDataIdOfDatasetToDisplay",
            dataMetaInfoForEmit.dataId
          );
          this.$router.push(
            `/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${this.currentReportingPeriod}`
          );
        }
      }
    },
  },
});
</script>
