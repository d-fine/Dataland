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
      <div class="col-7">
        <TaxonomyPanel :dataID="metaDataInfo.data[0].dataId" v-if="metaDataInfo.data.length > 0" />
      </div>
    </div>
  </div>
  <div v-else class="col-12 text-left">
    <h2>No EU Taxonomy Data Present</h2>
  </div>
</template>

<script>
import { ApiClientProvider } from "@/services/ApiClients";
import TaxonomyPanel from "@/components/resources/taxonomy/TaxonomyPanel";

export default {
  name: "TaxonomyData",
  components: { TaxonomyPanel },
  data() {
    return {
      response: null,
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
  inject: ["getKeycloakInitPromise"],
  methods: {
    async getCompanyInformation() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise(),
        ).getMetaDataControllerApi();
        this.metaDataInfo = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID, "EuTaxonomyData");
      } catch (error) {
        console.error(error);
        this.metaDataInfo = null;
      }
    },
  },
};
</script>
