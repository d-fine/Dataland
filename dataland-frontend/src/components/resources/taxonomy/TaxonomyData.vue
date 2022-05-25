<template>
  <div v-if="metaDataInfo && metaDataInfo.data.length > 0">
    <div class="grid">
      <div class="col-12 text-left">
        <h2 class="mb-0">EU Taxonomy Data</h2>
      </div>
      <div class="col-6 text-left">
        <p class="font-semibold m-0">
          2021
        </p>
        <p class="font-semibold text-gray-800 mt-0">
          Data from company report. Disclosures in accordance with EU Taxonomy Regulation, Article 8.
        </p>
      </div>
    </div>
    <div class="grid">
      <div class="col-6 text-left">
        <Button class="bg-white border-gray-50 border-2 text-900 mt-2 mb-3 h-3rem">
          <span>Invite someone to access this data.&nbsp;</span>
          <span class="font-semibold"> No registration necessary.</span>
          <span class="uppercase ml-4 text-primary font-semibold">
            SHARE
          </span>
          <i class="material-icons ml-2 text-primary " aria-hidden="true">share</i>

        </Button>
      </div>
    </div>
    <div class="grid">
      <div class="col-7">
        <TaxonomyPanel :dataID="metaDataInfo.data[0].dataId" v-if="metaDataInfo.data.length > 0"/>
      </div>
    </div>
  </div>
  <div v-else class="col-12 text-left">
    <h2>No EU Taxonomy Data Present</h2>
  </div>
</template>

<script>
import Button from "primevue/button";

import {axiosDefaultConfiguration} from "@/services/AxiosDefaultConfiguration"
import {MetaDataControllerApi} from "../../../../build/clients/backend/api";
import TaxonomyPanel from "@/components/resources/taxonomy/TaxonomyPanel";

const metaDataControllerApi = new MetaDataControllerApi(axiosDefaultConfiguration)

export default {
  name: "TaxonomyData",
  components: {TaxonomyPanel, Button},
  data() {
    return {
      response: null,
      metaDataInfo: null
    }
  },
  props: {
    companyID: {
      type: String
    }
  },
  created() {
    this.getCompanyInformation()
  },
  watch: {
    companyID() {
      this.getCompanyInformation()
    }
  },
  methods: {
    async getCompanyInformation() {
      try {
        this.metaDataInfo = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID, "EuTaxonomyData")
      } catch (error) {
        console.error(error)
        this.metaDataInfo = null
      }
    }
  }
}
</script>