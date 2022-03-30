<template>
  <div v-if="companyInfo" class="container">
    <div class="row">
      <div class="col m12 s12">
        <h2>Company Information about {{companyInfo.data.companyName}} (ID: {{companyInfo.data.companyId}})</h2>
        <ResultTable v-if="response" entity="Available Datasets" :data="response.data" route="/eutaxonomies/" :headers="['Data ID', 'Data Type']" linkKey="Data Type" linkID="Data ID" />
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
      companyInfo: null
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
        this.companyInfo = await companyStore.perform(this.companyID)
    },
    async getCompanyDataset() {
        this.response = await dataStore.perform(this.companyID)
    }
  }
}
</script>