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
    <div class="col-6">
      <Button class="bg-white border-gray-50 border-2 text-900">
        <span>Invite someone to access this data.&nbsp;</span>
        <span class="font-semibold"> No registration necessary.</span>
        <span class="uppercase ml-4 text-primary font-semibold"> SHARE <i class="pi pi-share-alt ml-2" aria-hidden="true"/> </span>
      </Button>
    </div>
  </div>
  <div class="grid">
    <div class="col-1 bg-green-50">1</div>
    <div class="col-1 bg-green-100">2</div>
    <div class="col-1 bg-green-200">3</div>
    <div class="col-1 bg-green-300">4</div>
    <div class="col-1 bg-green-400">5</div>
    <div class="col-1 bg-green-500">6</div>
    <div class="col-1 bg-green-600">7</div>
    <div class="col-1 bg-green-700">8</div>
    <div class="col-1 bg-green-800">9</div>
    <div class="col-1 bg-green-900">10</div>
    <div class="col-1 bg-green-50">11</div>
    <div class="col-1 bg-green-100">12</div>
  </div>
  <div v-if="false">
    <pre>{{metaDataInfo.data[0]}}</pre>
  </div>
</template>

<script>
import Button from "primevue/button";

import {DataStore} from "@/services/DataStore";
import {MetaDataControllerApi} from "@/../build/clients/backend";
const metaApi = new MetaDataControllerApi()
const metaStore = new DataStore(metaApi.getListOfDataMetaInfo)

export default {
  name: "EUData",
  components: {Button},
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