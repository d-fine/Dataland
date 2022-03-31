<template>
  <div class="grid" >
    <div v-if="response" class="col ">
      <h1>Company Data</h1>
      <div class="left-align">
      <h2 v-if="companyInfo">Company: {{companyInfo.data.companyName}}</h2>
      <h2>Dataset: {{$route.params.dataID}}</h2>

      </div>
      <div  class="col m12 s12">
        <Card>
          <template #title>
            <h4>EU Taxonomy Data</h4>
          </template>
          <template #content>
            <div class="grid">
              <div class="col md:col-4 col-offset-2">
                <TaxoCard title="Eligible Revenue" :amount='dataSet.Revenue.eligible'
                          :total='dataSet.Revenue.total'></TaxoCard>
                <br>
                <TaxoCard title="Eligible CapEx" :amount='dataSet.Capex.eligible'
                          :total='dataSet.Capex.total'></TaxoCard>
                <br>
                <TaxoCard title="Eligible OpEx" :amount='dataSet.Opex.eligible'
                          :total='dataSet.Opex.total'></TaxoCard>
              </div>
              <div class="col md:col-4 ">
                <TaxoCard title="Aligned Revenue" :amount='dataSet.Revenue.aligned'
                          :total='dataSet.Revenue.total'></TaxoCard>
                <br>
                <TaxoCard title="Aligned CapEx" :amount='dataSet.Capex.aligned'
                          :total='dataSet.Capex.total'></TaxoCard>
                <br>
                <TaxoCard title="Aligned OpEx" :amount='dataSet.Opex.aligned'
                          :total='dataSet.Opex.total'></TaxoCard>
              </div>
            </div>
          </template>
        </Card>
      </div>

    </div>
  </div>
</template>

<script>
import {EuTaxonomyDataControllerApi, CompanyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import TaxoCard from "@/components/ui/TaxoCard";
import Card from "primevue/card";
const euTaxonomyApi = new EuTaxonomyDataControllerApi()
const companyApi = new CompanyDataControllerApi()
const dataStore = new DataStore(euTaxonomyApi.getCompanyAssociatedDataSet)
const companyStore = new DataStore(companyApi.getCompanyById)

export default {
  name: "CompanyEU",
  components: {TaxoCard, Card},
  data() {
    return {
      response: null,
      dataSet: null,
      companyInfo: null
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
        this.response = await dataStore.perform(this.dataID)
        this.dataSet = this.response.data.data
        this.companyInfo = await companyStore.perform(this.response.data.companyId)

      } catch (error) {
        console.error(error)
      }
    },

  }
}
</script>