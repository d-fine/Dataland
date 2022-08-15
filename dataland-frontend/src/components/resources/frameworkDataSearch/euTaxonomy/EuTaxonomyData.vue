<template>
  <div v-if="getListOfMetaDataResponse && listOfMetaData.length > 0">
    <div class="grid">
      <div class="col-12 text-left">
        <h2 class="mb-0">EU Taxonomy Data</h2>
      </div>
      <div class="col-6 text-left">
        <p class="font-semibold m-0">2021</p>
        <p class="font-semibold text-gray-800 mt-0">Data from company report.</p>
      </div>
    </div>
    <div class="grid">
      <div
        class="col-7"
        v-if="listOfMetaData.length > 0 && firstEuTaxonomyDataMetaInfo.dataType === 'EuTaxonomyDataForNonFinancials'"
      >
        <EuTaxonomyPanelNonFinancials :dataID="firstEuTaxonomyDataMetaInfo.dataId" />
      </div>
      <div
        class="col-7"
        v-if="listOfMetaData.length > 0 && firstEuTaxonomyDataMetaInfo.dataType === 'EuTaxonomyDataForFinancials'"
      >
        <EuTaxonomyPanelFinancials :dataID="firstEuTaxonomyDataMetaInfo.dataId" />
      </div>
    </div>
  </div>
  <div v-else class="col-12 text-left">
    <h2>No EU Taxonomy Data Present</h2>
  </div>
</template>

<script>
import { ApiClientProvider } from "@/services/ApiClients";
import EuTaxonomyPanelNonFinancials from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyPanelNonFinancials";
import EuTaxonomyPanelFinancials from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyPanelFinancials";
import { DataMetaInformation } from "@/../build/clients/backend/api";

export default {
  name: "TaxonomyData",
  components: { EuTaxonomyPanelFinancials, EuTaxonomyPanelNonFinancials },
  data() {
    return {
      getListOfMetaDataResponse: null,
      listOfMetaData: new Array() < DataMetaInformation > [],
      firstEuTaxonomyDataMetaInfo: null,
    };
  },
  props: {
    companyID: {
      type: String,
    },
  },
  created() {
    this.getCompanyInformation();
  },
  watch: {
    companyID() {
      this.getCompanyInformation();
    },
  },
  inject: ["getKeycloakPromise"],
  methods: {
    async getCompanyInformation() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(this.getKeycloakPromise()).getMetaDataControllerApi();
        this.getListOfMetaDataResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        this.listOfMetaData = this.getListOfMetaDataResponse.data;
        this.firstEuTaxonomyDataMetaInfo = this.listOfMetaData.find((dataMetaInfo) =>
          dataMetaInfo.dataType.startsWith("EuTaxonomyDataFor")
        );
      } catch (error) {
        console.error(error);
        this.getListOfMetaDataResponse = null;
      }
    },
  },
};
</script>
