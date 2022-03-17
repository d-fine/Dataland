<template>
  <div class="container">
    <div class="row">
      <h1>EU Taxonomy Data</h1>
      <h2>Dataset: {{ $route.params.dataID }}</h2>
      <div v-if="response" class="col m12 s12">
        <div class="card">
          <div class="card-title left-align">
            <h4>Revenue</h4>
          </div>
          <div class="card-content">
            <div class="row">
              <div class="col m6">
                <TaxoCard title="Eligible Revenue" :amount='response.data.Revenue.eligible_turnover'
                          :total='response.data.Revenue.total'></TaxoCard>
                <TaxoCard title="Eligible CapEx" :amount='response.data.Capex.eligible_turnover'
                          :total='response.data.Capex.total'></TaxoCard>
                <TaxoCard title="Eligible OpEx" :amount='response.data.Opex.eligible_turnover'
                          :total='response.data.Opex.total'></TaxoCard>
              </div>
              <div class="col m6">
                <TaxoCard title="Aligned Revenue" :amount='response.data.Revenue.aligned_turnover'
                          :total='response.data.Revenue.total'></TaxoCard>
                <TaxoCard title="Aligned CapEx" :amount='response.data.Capex.aligned_turnover'
                          :total='response.data.Capex.total'></TaxoCard>
                <TaxoCard title="Aligned OpEx" :amount='response.data.Opex.aligned_turnover'
                          :total='response.data.Opex.total'></TaxoCard>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import {EuTaxonomyDataControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";
import TaxoCard from "@/components/ui/TaxoCard";

const api = new EuTaxonomyDataControllerApi()
const dataStore = new DataStore(api.getDataSet)
export default {
  name: "CompanyEU",
  components: {TaxoCard},
  data() {
    return {
      response: null
    }
  },
  props: {
    dataID: {
      type: Number
    }
  },
  created() {
    this.getCompanyEUDataset()
  },
  methods: {
    async getCompanyEUDataset() {
      try {
        this.response = await dataStore.perform(this.dataID, {baseURL: process.env.VUE_APP_API_URL})
        console.log(this.response)
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>