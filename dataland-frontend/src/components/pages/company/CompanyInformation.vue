<template>
  <div v-if="companyInfo" class="container">
    <div class="row">
      <div class="col m12 s12">
        <h2>Company Information about {{companyInfo.data.companyName}} (ID: {{companyInfo.data.companyId}})</h2>
        <ResultTable v-if="response" entity="Available Datasets" :data="response.data" route="/eutaxonomies/" :headers="['Data ID', 'Data Type', 'Data Insight']" linkkey="Data ID"/>
      </div>
    </div>
  </div>
</template>

<script>
import {CompanyDataControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";
import ResultTable from "@/components/ui/ResultTable";
const api = new CompanyDataControllerApi()
const dataStore = new DataStore(api.getCompanyDataSets)
const companyStore = new DataStore(api.getCompanyById)
export default {
  name: "CompanyInformation",
  components: {ResultTable},
  data() {
    return {
      response: null,
      companyInfo: null
    }
  },
  props:{
    companyID: {
      default: 1,
      type: Number
    }
  },
  created() {
    this.getCompanyDataset()
    this.getCompanyInformation()
  },
  methods: {
    async getCompanyInformation() {
      try {
        this.companyInfo = await companyStore.perform(this.companyID, {baseURL: process.env.VUE_APP_API_URL})
      } catch (err){
        console.error(err)
      }

    },
    async getCompanyDataset() {
      try {
        this.response = await dataStore.perform(this.companyID, {baseURL: process.env.VUE_APP_API_URL})
        console.log(this.response)
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>