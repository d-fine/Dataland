<template>
  <div v-if="company" class="container">
    <div class="row">
      <div class="col m12 s12">
        <h2>Company Information about {{ companyInformation.companyName }} (ID: {{ company.data.companyId }})</h2>
        <p>market cap: {{ companyInformation.marketCap }}</p>
        <p>reporting Date Of MarketCap: {{ companyInformation.reportingDateOfMarketCap }}</p>
        <p>headquarters: {{ companyInformation.headquarters }}</p>
        <p>Industrial Sector: {{ companyInformation.industrialSector }}</p>
        <ResultTable v-if="response" entity="Available Datasets" :data="response.data" route="/eutaxonomies/"
                     :headers="['Data ID', 'Data Type']" linkKey="Data Type" linkID="Data ID"/>
      </div>
    </div>
  </div>
</template>

<script>
import {CompanyDataControllerApi, MetaDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import ResultTable from "@/components/ui/ResultTable";

const companyApi = new CompanyDataControllerApi()
const metaDataApi = new MetaDataControllerApi()
const dataStore = new DataStore(metaDataApi.getListOfDataMetaInfo)
const companyStore = new DataStore(companyApi.getCompanyById)
export default {
  name: "CompanyInformation",
  components: {ResultTable},
  data() {
    return {
      response: null,
      company: null,
      companyInformation: null
    }
  },
  props: {
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
      this.company = await companyStore.perform(this.companyID)
      this.companyInformation = this.company.data.companyInformation
      console.log(this.companyInformation)
      console.log(this.companyInformation.companyName)
      console.log(this.company.data.companyId)
    },
    async getCompanyDataset() {
      this.response = await dataStore.perform(this.companyID, "")
    }
  }
}
</script>