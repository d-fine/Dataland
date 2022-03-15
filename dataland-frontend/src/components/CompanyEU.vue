<template>
  <div class="container">
    <div class="row">
      <h1>The user is {{ $route.params.companyID }}</h1>
      <div v-if="response" class="col m9">
        <div class="card">
          <div class="card-title left-align">
            <h4>Revenue</h4>
          </div>
          <div class="card-content">
            <div class="row">
              <div class="col m6">
                <TaxoCard title="Eligble Revenue" :amount='response.data["Revenues"]["Amount €"]'
                          :percent='response.data["Revenues"]["Taxonomy-aligned proportion of turnover %"]'></TaxoCard>
                <TaxoCard title="Eligble CapEx" :amount='response.data["Capex"]["Amount €"]'
                          :percent='response.data["Capex"]["Taxonomy-aligned proportion of turnover %"]'></TaxoCard>
                <TaxoCard title="Eligble OpEx" :amount='response.data["Opex"]["Amount €"]'
                          :percent='response.data["Opex"]["Taxonomy-aligned proportion of turnover %"]'></TaxoCard>
              </div>
              <div class="col m6">
                <TaxoCard title="Aligned Revenue" :amount='response.data["Revenues"]["Amount €"]'
                          :percent='response.data["Revenues"]["Taxonomy-aligned proportion of turnover %"]'></TaxoCard>
                <TaxoCard title="Aligned CapEx" :amount='response.data["Capex"]["Amount €"]'
                          :percent='response.data["Capex"]["Taxonomy-aligned proportion of turnover %"]'></TaxoCard>
                <TaxoCard title="Aligned OpEx" :amount='response.data["Opex"]["Amount €"]'
                          :percent='response.data["Opex"]["Taxonomy-aligned proportion of turnover %"]'></TaxoCard>
              </div>
            </div>

          </div>

        </div>
        <p>{{ response.data }}</p>
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
    companyID: {
      type: Number
    }
  },
  created() {
    this.getCompanyEUDataset()
  },
  methods: {
    async getCompanyEUDataset() {
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

<style scoped>

</style>