<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading SFDR Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="kpiDataObjects.length && !waitingForData">
    <CompanyDataTable
      :kpiDataObjects="kpiDataObjects"
      :dataDateOfDataSets="listOfDataDateToDisplayAsColumns"
      :kpiNameMappings="sfdrKpisNameMappings"
      :kpiInfoMappings="sfdrKpisInfoMappings"
      :subAreaNameMappings="sfdrSubAreasNameMappings"
      tableDataTitle="SFDR data"
    />
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { SfdrData, DataAndMetaInformationSfdrData, DataMetaInformation } from "@clients/backend";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { sortDatesToDisplayAsColumns } from "@/utils/DataTableDisplay";
import CompanyDataTable from "@/components/general/CompanyDataTable.vue";
import {
  sfdrSubAreasNameMappings,
  sfdrKpisNameMappings,
  sfdrKpisInfoMappings,
} from "@/components/resources/frameworkDataSearch/sfdr/DataModelsTranslations";

export default defineComponent({
  name: "SfdrPanel",
  components: { CompanyDataTable },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      waitingForData: true,
      sfdrDataAndMetaInfo: [] as Array<DataAndMetaInformationSfdrData>,
      listOfDataDateToDisplayAsColumns: [] as Array<{ dataId: string; dataDate: string }>,
      kpiDataObjects: [] as { [index: string]: string | object; subAreaKey: string; kpiKey: string }[],
      sfdrKpisNameMappings,
      sfdrKpisInfoMappings,
      sfdrSubAreasNameMappings,
    };
  },
  props: {
    companyId: {
      type: String,
    },
    singleDataMetaInfoToDisplay: {
      type: Object as () => DataMetaInformation,
    },
  },
  created() {
    void this.fetchData();
  },
  methods: {
    /**
     * Fetches all Sfdr datasets for the current company and converts them to the requried frontend format.
     */
    async fetchData() {
      try {
        this.waitingForData = true;
        const sfdrDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getSfdrDataControllerApi();

        if (this.singleDataMetaInfoToDisplay) {
          const singleSfdrData = (
            await sfdrDataControllerApi.getCompanyAssociatedSfdrData(this.singleDataMetaInfoToDisplay.dataId)
          ).data.data as SfdrData; // TODO think about catching errors here,   take dataMetaInfo fetch in the base-component as example

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
     *
     * @param kpiKey The field name of a kpi
     * @param kpiValue The corresponding value to the kpiKey
     * @param subAreaKey The sub area to which the kpi belongs
     * @param dataIdOfSfdrDataset The value of the data Id of an Sfdr dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: object | string,
      subAreaKey: string,
      dataIdOfSfdrDataset: string
    ): void {
      let indexOfExistingItem = -1;
      const kpiDataObject = {
        subAreaKey: subAreaKey == "general" ? `_${subAreaKey}` : subAreaKey,
        kpiKey: kpiKey,
        [dataIdOfSfdrDataset]: kpiValue,
      };
      indexOfExistingItem = this.kpiDataObjects.findIndex(
        (singleKpiDataObject) => singleKpiDataObject.kpiKey === kpiKey
      );
      if (indexOfExistingItem !== -1) {
        Object.assign(this.kpiDataObjects[indexOfExistingItem], kpiDataObject);
      } else {
        this.kpiDataObjects.push(kpiDataObject);
      }
    },

    /**
     * Retrieves and converts values from an array of SDFR datasets in order to make it displayable in the frontend.
     *
     */
    convertSfdrDataToFrontendFormat(): void {
      if (this.sfdrDataAndMetaInfo.length) {
        this.sfdrDataAndMetaInfo.forEach((oneSfdrDataset: DataAndMetaInformationSfdrData) => {
          const dataIdOfSfdrDataset = oneSfdrDataset.metaInfo?.dataId ?? "";
          const dataDateOfSfdrDataset = oneSfdrDataset.data.social?.general?.fiscalYearEnd ?? "";
          this.listOfDataDateToDisplayAsColumns.push({
            dataId: dataIdOfSfdrDataset,
            dataDate: dataDateOfSfdrDataset,
          });
          for (const areaObject of Object.values(oneSfdrDataset.data)) {
            for (const [subAreaKey, subAreaObject] of Object.entries(areaObject as object) as [string, object][]) {
              for (const [kpiKey, kpiValue] of Object.entries(subAreaObject) as [string, object][]) {
                this.createKpiDataObjects(kpiKey, kpiValue, subAreaKey, dataIdOfSfdrDataset);
              }
            }
          }
        });
      }
      this.listOfDataDateToDisplayAsColumns = sortDatesToDisplayAsColumns(this.listOfDataDateToDisplayAsColumns);
    },
  },
  watch: {
    companyId() {
      this.listOfDataDateToDisplayAsColumns = [];
      void this.fetchData();
    },
  },
});
</script>
