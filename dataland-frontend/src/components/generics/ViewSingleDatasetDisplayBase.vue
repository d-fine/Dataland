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
        :placeholder="currentReportingPeriod"
        aria-label="Choose reporting period"
        class="fill-dropdown"
        dropdownIcon="pi pi-angle-down"
        @change=""
      />
    </template>

    <template v-slot:content>
      <div v-if="foundDataIdToDisplay">
        <div v-if="foundDataIdBelongsToOutdatedDataset">
          this dataset is outdated
          <PrimeButton @click="switchToLatestVersion"> See latest version </PrimeButton>
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
import Dropdown from "primevue/dropdown";
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

  watch: {
    chosenReportingPeriodInDropdown(newReportingPeriod) {
      const listOfDataMetaInfoForChosenReportingPeriod = this.listOfReceivedActiveDataMetaInfo.filter(
        (dataMetaInfo) => dataMetaInfo.reportingPeriod == newReportingPeriod
      );
      this.$emit(
        "updateDataIdOfDatasetToDisplay",
        this.getActiveDataMetaInfoFromListOfDataMetaInfoForSingleReportingPeriod(
          listOfDataMetaInfoForChosenReportingPeriod
        ).dataId
      );
      this.$router.push(
        `/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${newReportingPeriod}`
      ); //TODO duplice code
    },
  },

  methods: {
    switchToLatestVersion() {
      const aa = this.listOfReceivedActiveDataMetaInfo.filter(
        (dataMetaInfo) => dataMetaInfo.reportingPeriod == this.currentReportingPeriod
      )[0]; // TODO list needs to have 1 element! check!
      console.log(aa); //TODO variable name
      this.foundDataIdBelongsToOutdatedDataset = false;
      this.$emit("updateDataIdOfDatasetToDisplay", aa.dataId);
      this.$router.push(
        `/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${this.chosenReportingPeriodInDropdown}`
      );
    },

    getActiveDataMetaInfoFromListOfDataMetaInfoForSingleReportingPeriod(
      listOfDataMetaInfoForSingleReportingPeriod: DataMetaInformation[]
    ): DataMetaInformation {
      // TODO happens currently based on upload time => in the future this could just check for a status: "active" e.g.
      const a = listOfDataMetaInfoForSingleReportingPeriod.filter((dataMetaInfo) => dataMetaInfo.currentlyActive); // TODO rename "a"
      if (a.length == 1) {
        return a[0];
      } else if (a.length == 0) {
        throw TypeError(`The fetched data meta info for the current company ID ${this.companyId}
        and framework ${this.dataType} is empty.`);
      } else {
        throw TypeError(`The fetched data meta info for the current company ID ${this.companyId}
        and framework ${this.dataType} has several data meta info datsets with the status 'active'.
        Therefore no unique data ID could be determined.`);
      }
    },

    handleUpdateAvailableReportingPeriods(listOfAvailableReportingPeriods: string[]) {
      this.reportingPeriodsInDropdown = listOfAvailableReportingPeriods;
      console.log("reportingPeriods are"); // TODO debugging
      console.log(this.reportingPeriodsInDropdown); // TODO debugging
    },

    handleUpdateActiveDataMetaInfo(listOfReceivedDataMetaInfo: Array<DataMetaInformation>) {
      this.listOfReceivedActiveDataMetaInfo = listOfReceivedDataMetaInfo;
      console.log("receivedActiveDataMetainfo are"); // TODO debugging
      console.log(this.listOfReceivedActiveDataMetaInfo); // TODO debugging
      this.chooseDataMetaInfoForDisplayedDataset();
      this.waitingForDataMetaInfoAndChoosingDatasetToDisplay = false;
    },

    getActiveDataIdFromLatestReportingPeriodIfNumberFound(): string {
      const numbersInReportingPeriodsAsStrings = this.reportingPeriodsInDropdown.filter(
        (reportingPeriod) => !isNaN(parseInt(reportingPeriod))
      );
      console.log("------------------------------------");
      console.log(numbersInReportingPeriodsAsStrings);
      if (numbersInReportingPeriodsAsStrings.length > 0) {
        const integersInReportingPeriods = numbersInReportingPeriodsAsStrings.map((integerAsString) =>
          parseInt(integerAsString)
        );
        const latestReportingPeriod = integersInReportingPeriods.reduce((a, b) => Math.max(a, b)).toString();
        this.currentReportingPeriod = latestReportingPeriod;
        console.log("found latest reporting period"); // TODO debugging
        console.log("------------------------------------");
        // assure that active TODO  else throw an error
        return this.getActiveDataMetaInfoFromListOfDataMetaInfoForSingleReportingPeriod(
          this.listOfReceivedActiveDataMetaInfo.filter(
            (dataMetaInfo: DataMetaInformation) => dataMetaInfo.reportingPeriod == latestReportingPeriod
          )
        ).dataId;
      } else {
        console.log("could not found latest reporting period, returning first as default"); // TODO debugging
        console.log("------------------------------------");
        return this.listOfReceivedActiveDataMetaInfo[0].dataId;
      }
    },

    /**
     * TODO adjust: Displays either the data set using the ID from the query param or if that is not available the first data set from the list of received data sets.
     */
    async chooseDataMetaInfoForDisplayedDataset() {
      if (this.dataId) {
        console.log("dataId passed in Url"); // TODO debugging
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
        console.log("only reporting period passed in Url"); // TODO debugging
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
        console.log("no dataId or reprtingPeriod in Url => default"); // TODO debugging
        this.foundDataIdToDisplay = true; // TODO think about this later again
        this.foundDataIdBelongsToOutdatedDataset = false;
        this.$emit(
          // TODO duplicate code block more or less => put in own function
          "updateDataIdOfDatasetToDisplay",
          this.getActiveDataIdFromLatestReportingPeriodIfNumberFound()
        );
        this.$router.push(
          `/companies/${this.companyId}/frameworks/${this.dataType}/reportingPeriods/${this.currentReportingPeriod}`
        );
      }
    },
  },
});
</script>
