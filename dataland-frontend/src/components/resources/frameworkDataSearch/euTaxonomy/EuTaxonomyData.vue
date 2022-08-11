<template>
  <div v-if="metaDataInfo && metaDataInfo.data.length > 0">
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
        v-if="metaDataInfo.data.length > 0 && metaDataInfo.data[0].dataType === 'EuTaxonomyDataForNonFinancials'"
      >
        <EuTaxonomyPanelNonFinancials :dataID="metaDataInfo.data[0].dataId" />
      </div>
      <div
        class="col-7"
        v-if="metaDataInfo.data.length > 0 && metaDataInfo.data[0].dataType === 'EuTaxonomyDataForFinancials'"
      >
        <EuTaxonomyPanelFinancials :dataID="metaDataInfo.data[0].dataId" />
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

export default {
  name: "TaxonomyData",
  components: { EuTaxonomyPanelFinancials, EuTaxonomyPanelNonFinancials },
  data() {
    return {
      metaDataInfo: null,
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
        this.metaDataInfo = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
      } catch (error) {
        console.error(error);
        this.metaDataInfo = null;
      }
    },
  },
};
</script>
