<template>
  <ViewFrameworkBase
    :companyID="companyId"
    :dataType="dataType"
    :singleDataMetaInfoToDisplay="singleDataMetaInfoToDisplay"
    @updateActiveDataMetaInfoForChosenFramework="handleUpdateActiveDataMetaInfo"
    :viewInPreviewMode="viewInPreviewMode"
  >
    <template v-slot:content="slotProps">
      <div v-if="isListOfDataIdsToDisplayFound">
        <DatasetDisplayStatusIndicator
          :displayed-dataset="singleDataMetaInfoToDisplay"
          :received-map-of-reporting-periods-to-active-data-meta-info="
            receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo
          "
          :is-multiview="true"
        />

        <div class="grid">
          <div class="col-12 text-left">
            <h2 class="mb-0" data-test="frameworkDataTableTitle">{{ humanizeString(dataType) }}</h2>
          </div>
          <div class="col-12">
            <MultiLayerDataTableFrameworkPanel
              v-if="dataType === DataTypeEnum.EutaxonomyFinancials"
              :frameworkIdentifier="DataTypeEnum.EutaxonomyFinancials"
              :companyId="companyId"
              :display-configuration="configForEuTaxonomyFinancialsMLDT"
              :singleDataMetaInfoToDisplay="singleDataMetaInfoToDisplay"
              :inReviewMode="slotProps.inReviewMode"
              data-test="MultiLayerDataTableFrameworkPanelFinancials"
            />
            <MultiLayerDataTableFrameworkPanel
              v-if="dataType === DataTypeEnum.P2p"
              :frameworkIdentifier="DataTypeEnum.P2p"
              :companyId="companyId"
              :display-configuration="convertDataModelToMLDTConfig(p2pDataModel)"
              :singleDataMetaInfoToDisplay="singleDataMetaInfoToDisplay"
              :inReviewMode="slotProps.inReviewMode"
              data-test="MultiLayerDataTableFrameworkPanelP2P"
            />
            <MultiLayerDataTableFrameworkPanel
              v-if="frameworkViewConfiguration?.type == 'MultiLayerDataTable'"
              :frameworkIdentifier="dataType"
              :companyId="companyId"
              :display-configuration="frameworkViewConfiguration!!.configuration"
              :singleDataMetaInfoToDisplay="singleDataMetaInfoToDisplay"
              :inReviewMode="slotProps.inReviewMode"
              data-test="MultiLayerDataTableFrameworkPanelOthers"
            />
          </div>
        </div>
      </div>
      <div v-if="isWaitingForListOfDataIdsToDisplay" class="col-12 text-left">
        <h2>Checking if {{ humanizeString(dataType) }} data available...</h2>
      </div>
      <div
        v-if="!isListOfDataIdsToDisplayFound && !isWaitingForListOfDataIdsToDisplay"
        class="col-12 text-left"
        data-test="noDataForThisFrameworkPresentErrorIndicator"
      >
        <h2>No {{ humanizedDataDescription }} data present for this company.</h2>
      </div>
      <div v-if="isDataIdInUrlInvalid" data-test="noDataForThisDataIdPresentErrorIndicator">
        <h2>
          No {{ humanizedDataDescription }} data could be found for the data ID passed in the URL for this company and
          framework.
        </h2>
      </div>
      <div v-if="isReportingPeriodInUrlInvalid" data-test="noDataForThisReportingPeriodPresentErrorIndicator">
        <h2>
          No {{ humanizedDataDescription }} data could be found for the reporting period passed in the URL for this
          company.
        </h2>
      </div>
    </template>
  </ViewFrameworkBase>
</template>

<script lang="ts">
// @ts-nocheck
import ViewFrameworkBase from '@/components/generics/ViewFrameworkBase.vue';
import { defineComponent, inject } from 'vue';
import { type DataMetaInformation, DataTypeEnum } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import DatasetDisplayStatusIndicator from '@/components/resources/frameworkDataSearch/DatasetDisplayStatusIndicator.vue';
import MultiLayerDataTableFrameworkPanel from '@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableFrameworkPanel.vue';
import { convertDataModelToMLDTConfig } from '@/components/resources/dataTable/conversion/MultiLayerDataTableConfigurationConverter';
import { p2pDataModel } from '@/components/resources/frameworkDataSearch/p2p/P2pDataModel';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import {
  type FrontendFrameworkDefinition,
  type FrameworkViewConfiguration,
} from '@/frameworks/BaseFrameworkDefinition';
import { configForEuTaxonomyFinancialsMLDT } from '@/components/resources/frameworkDataSearch/euTaxonomy/configForEutaxonomyFinancialsMLDT';

export default defineComponent({
  name: 'ViewMultipleDatasetsDisplayBase',
  computed: {
    p2pDataModel() {
      return p2pDataModel;
    },
    frameworkConfiguration(): FrontendFrameworkDefinition<unknown> | undefined {
      return this.dataType ? getFrontendFrameworkDefinition(this.dataType) : undefined;
    },
    frameworkViewConfiguration(): FrameworkViewConfiguration<unknown> | undefined {
      return this.frameworkConfiguration?.getFrameworkViewConfiguration();
    },
    configForEuTaxonomyFinancialsMLDT() {
      return configForEuTaxonomyFinancialsMLDT;
    },
  },
  components: {
    MultiLayerDataTableFrameworkPanel,
    DatasetDisplayStatusIndicator,
    ViewFrameworkBase,
  },
  props: {
    companyId: {
      type: String,
      required: true,
    },
    dataType: {
      type: String,
      required: true,
    },
    dataId: {
      type: String,
    },
    reportingPeriod: {
      type: String,
    },
    viewInPreviewMode: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      isWaitingForListOfDataIdsToDisplay: true,
      isListOfDataIdsToDisplayFound: false,
      receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo: {} as Map<string, DataMetaInformation>,
      singleDataMetaInfoToDisplay: null as null | DataMetaInformation,
      humanizeString: humanizeStringOrNumber,
      isDataIdInUrlInvalid: false,
      isReportingPeriodInUrlInvalid: false,
      humanizedDataDescription: humanizeStringOrNumber(this.dataType),
      DataTypeEnum,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  watch: {
    dataId(newDataId: string) {
      if (newDataId) {
        this.setFlagsToDataNotFoundState();
        void this.getMetaDataForDataId(newDataId);
      } else if (!this.reportingPeriod) {
        this.setSingleDataMetaInfoToDisplay(null);
      }
    },
    reportingPeriod(newReportingPeriod: string) {
      if (newReportingPeriod) {
        const dataMetaInfoForNewlyChosenReportingPeriod =
          this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(newReportingPeriod);
        if (dataMetaInfoForNewlyChosenReportingPeriod) {
          this.getMetaDataForDataId(dataMetaInfoForNewlyChosenReportingPeriod.dataId).catch((err) =>
            console.log(
              'Retrieving meta data information for data ID ' +
                dataMetaInfoForNewlyChosenReportingPeriod.dataId +
                ' failed with error ' +
                String(err)
            )
          );
        } else {
          this.isReportingPeriodInUrlInvalid = true;
        }
      } else if (!this.dataId) {
        this.setSingleDataMetaInfoToDisplay(null);
      }
    },
  },

  methods: {
    convertDataModelToMLDTConfig,
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
     * Method to handle an invalid data ID that was passed in URL
     */
    handleInvalidDataIdPassedInUrl() {
      this.isDataIdInUrlInvalid = true;
      this.isListOfDataIdsToDisplayFound = false;
    },

    /**
     * Method to handle an invalid reporting period that was passed in URL
     */
    handleInvalidReportingPeriodPassedInUrl() {
      this.isReportingPeriodInUrlInvalid = true;
      this.isListOfDataIdsToDisplayFound = false;
    },

    /**
     * Method to set a data meta information object as the only one to display
     * @param dataMetaInfoToDisplay the data meta information to display
     */
    setSingleDataMetaInfoToDisplay(dataMetaInfoToDisplay: DataMetaInformation | null) {
      this.setFlagsToDataFoundState();
      this.singleDataMetaInfoToDisplay = dataMetaInfoToDisplay;
    },

    /**
     * Method to asynchronously retrieve the meta data associated to a given data ID
     * @param dataId the data id to retrieve meta info for
     */
    async getMetaDataForDataId(dataId: string) {
      try {
        const backendClients = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients;
        const metaDataControllerApi = backendClients.metaDataController;
        const apiResponse = await metaDataControllerApi.getDataMetaInfo(dataId);
        const dataMetaInfoForDataSetWithDataIdFromUrl = apiResponse.data;
        if (
          dataMetaInfoForDataSetWithDataIdFromUrl.companyId != this.companyId ||
          dataMetaInfoForDataSetWithDataIdFromUrl.dataType != this.dataType
        ) {
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
        await this.getMetaDataForDataId(this.dataId);
      } else if (!this.dataId && this.reportingPeriod) {
        const activeDataMetaInfoWithReportingPeriodFromUrl =
          this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo.get(this.reportingPeriod);
        if (activeDataMetaInfoWithReportingPeriodFromUrl) {
          this.setSingleDataMetaInfoToDisplay(activeDataMetaInfoWithReportingPeriodFromUrl);
        } else {
          this.handleInvalidReportingPeriodPassedInUrl();
        }
      } else {
        this.setFlagsToDataFoundState();
      }
    },

    /**
     * Stores the received map of distinct reporting periods to active meta info from the
     * "updateActiveDataMetaInfoForChosenFramework" event, then triggers a controller-method which picks the data
     * meta infos for the datasets to display, and finally terminates the loading-state of the component.
     * @param receivedMapOfReportingPeriodsToActiveDataMetaInfo 1-to-1 map between reporting periods and corresponding
     * active data meta information objects
     */
    handleUpdateActiveDataMetaInfo(
      receivedMapOfReportingPeriodsToActiveDataMetaInfo: Map<string, DataMetaInformation>
    ) {
      this.receivedMapOfDistinctReportingPeriodsToActiveDataMetaInfo =
        receivedMapOfReportingPeriodsToActiveDataMetaInfo;
      this.createListOfDataMetaInfoForDisplayedDatasets().catch((err) =>
        console.log('Retrieving data meta info failed with error ' + String(err))
      );
      this.isWaitingForListOfDataIdsToDisplay = false;
    },
  },
});
</script>
