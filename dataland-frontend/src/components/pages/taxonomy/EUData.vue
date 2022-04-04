<template>
  <div class="grid">
    <div class="col-12 text-left">
      <h2>EU Taxonomy Data</h2>
    </div>
    <div class="col-6 text-left">
      <span class="font-semibold text-gray-800">Complete dataset for reporting according
        to EU Taxonomy Regulation, Article 8. For 2022 requirements.
      </span>
    </div>
  </div>
  <div class="grid">
    <div class="col-6 text-left">
      <Button class="bg-white border-gray-50 border-2 text-900">
        <span>Invite someone to access this data.&nbsp;</span>
        <span class="font-semibold"> No registration necessary.</span>
        <span class="uppercase ml-4 text-primary font-semibold"> SHARE <i class="pi pi-share-alt ml-2" aria-hidden="true"/> </span>
      </Button>
    </div>
  </div>
  <div class="grid">
    <div class="col-2 text-left">
      <span class="font-semibold">NFRD required: </span>
      <span>No</span>
    </div>
    <div class="col-2 text-left">
      <span class="font-semibold">Level of Assurance: </span>
      <span>Reasonable</span>
    </div>
  </div>
  <GridHelper/>
  <div v-if="false">
    <pre>{{metaDataInfo.data[0]}}</pre>
  </div>
</template>

<script>
import Button from "primevue/button";

import {DataStore} from "@/services/DataStore";
import {MetaDataControllerApi} from "@/../build/clients/backend";
import GridHelper from "@/components/helper/GridHelper";
const metaApi = new MetaDataControllerApi()
const metaStore = new DataStore(metaApi.getListOfDataMetaInfo)

export default {
  name: "EUData",
  components: {GridHelper, Button},
  data() {
    return {
      response: null,
      metaDataInfo: null
    }
  },
  props: {
    companyID: {
      default: 1,
      type: Number
    }
  },
  created() {
    this.getCompanyInformation()
  },
  methods: {
    async getCompanyInformation() {
      this.metaDataInfo = await metaStore.perform(this.companyID, "EuTaxonomyData")
    }
  }
}
</script>

<style scoped>

</style>