<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading SFDR Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="kpiDataObjects.length && !waitingForData">
    <CompanyDataTable
      :kpiDataObjects="kpiDataObjects"
      :reportingPeriodsOfDataSets="listOfColumnIdentifierObjects"
      :kpiNameMappings="sfdrKpisNameMappings"
      :kpiInfoMappings="sfdrKpisInfoMappings"
      :subAreaNameMappings="sfdrSubAreasNameMappings"
    />
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import {
  CompanyReport,
  DataAndMetaInformationSfdrData,
  SfdrData,
  SfdrEnvironmental,
  SfdrSocial,
} from "@clients/backend";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import CompanyDataTable from "@/components/general/CompanyDataTable.vue";
import {
  sfdrKpisInfoMappings,
  sfdrKpisNameMappings,
  sfdrSubAreasNameMappings,
} from "@/components/resources/frameworkDataSearch/sfdr/DataModelsTranslations";
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";

export default defineComponent({
  name: "SfdrPanel",
  components: { CompanyDataTable },
  data() {
    return {
      firstRender: true,
      waitingForData: true,
      sfdrDataAndMetaInfo: [] as Array<DataAndMetaInformationSfdrData>,
      listOfColumnIdentifierObjects: [] as Array<{ dataId: string; reportingPeriod: string }>,
      kpiDataObjects: [] as { [index: string]: string | object; subAreaKey: string; kpiKey: string }[],
      sfdrKpisNameMappings,
      sfdrKpisInfoMappings,
      sfdrSubAreasNameMappings,
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.listOfColumnIdentifierObjects = [];
      void this.fetchSfdrData();
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.listOfColumnIdentifierObjects = [];
        void this.fetchSfdrData();
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    void this.fetchSfdrData();
    this.firstRender = false;
  },
  methods: {
    /**
     * Fetches all accepted Sfdr datasets for the current company and converts them to the required frontend format.
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
        this.convertSfdrDataToFrontendFormat();
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * Creates kpi data objects to pass them to the data table.
     * @param kpiKey The field name of a kpi
     * @param kpiValue The corresponding value to the kpiKey
     * @param subAreaKey The sub area to which the kpi belongs
     * @param dataIdOfSfdrDataset The value of the data Id of an Sfdr dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: object | string,
      subAreaKey: string,
      dataIdOfSfdrDataset: string,
    ): void {
      let indexOfExistingItem = -1;
      const kpiDataObject = {
        subAreaKey: subAreaKey == "general" ? `_${subAreaKey}` : subAreaKey,
        kpiKey: kpiKey,
        [dataIdOfSfdrDataset]: kpiValue,
      };
      indexOfExistingItem = this.kpiDataObjects.findIndex(
        (singleKpiDataObject) => singleKpiDataObject.kpiKey === kpiKey,
      );
      if (indexOfExistingItem !== -1) {
        Object.assign(this.kpiDataObjects[indexOfExistingItem], kpiDataObject);
      } else {
        this.kpiDataObjects.push(kpiDataObject);
      }
    },

    /**
     * @param oneSfdrDataset sfdr dataset with meta information
     * @returns an object with data ID and reporting period
     */
    createColumnIdentifierObject(oneSfdrDataset: DataAndMetaInformationSfdrData) {
      const dataIdOfSfdrDataset = oneSfdrDataset.metaInfo?.dataId ?? "";
      const reportingPeriodOfSfdrDataset = oneSfdrDataset.metaInfo?.reportingPeriod ?? "";
      return {
        dataId: dataIdOfSfdrDataset,
        reportingPeriod: reportingPeriodOfSfdrDataset,
      };
    },

    /**
     *
     * @param sfdrData Data object from DataAndMetaInformationSfdrData
     * @param columnIdentifierDataId key name of the SFDR dataset property
     */
    createKpiDataObjectsForSfdrDataProps(sfdrData: SfdrData, columnIdentifierDataId: string) {
      const dataEntries = Object.entries(sfdrData).map((dataEntry) => {
        if (dataEntry[1] == null) {
          dataEntry[1] = "";
        }
        return dataEntry;
      });
      dataEntries.forEach((dataEntry: [string, SfdrSocial | SfdrEnvironmental | { [key: string]: CompanyReport }]) => {
        const [sfdrDataPropName, sfdrDataPropValue] = dataEntry;
        Object.entries(sfdrDataPropValue).forEach((propValue: [string, object | string]) => {
          const [kpiKey, kpiValue] = propValue;
          this.createKpiDataObjects(kpiKey, kpiValue, sfdrDataPropName, columnIdentifierDataId);
        });
      });
    },

    /**
     * Retrieves and converts values from an array of SFDR datasets in order to make it displayable in the frontend.
     *
     */
    convertSfdrDataToFrontendFormat() {
      const mappedOfColumnIdentifierObjects = this.sfdrDataAndMetaInfo.map(
        (oneSfdrDataset: DataAndMetaInformationSfdrData) => {
          const columnIdentifier = this.createColumnIdentifierObject(oneSfdrDataset);
          Object.values(oneSfdrDataset.data)
            .filter((sfdrData: SfdrData) => sfdrData !== null)
            .forEach((sfdrData: SfdrData) => {
              this.createKpiDataObjectsForSfdrDataProps(sfdrData, columnIdentifier.dataId);
            });
          return columnIdentifier;
        },
      );
      this.listOfColumnIdentifierObjects = sortReportingPeriodsToDisplayAsColumns(mappedOfColumnIdentifierObjects);
    },
  },
});
</script>
