<template>
  <div class="container" >
    <div v-if="response" class="row">
      <h1>Company Data</h1>
      <div class="left-align">
      <h2 v-if="companyInfo">Company: {{companyInfo.data.companyName}}</h2>
      <h2>Dataset: {{$route.params.dataID}}</h2>

      </div>
      <div  class="col m12 s12">
        <div class="card">
          <div class="card-title left-align">
            <h4>EU Taxonomy Data</h4>
          </div>
          <div class="card-content">
            <div class="row">
              <div class="col m6">
                <TaxoCard title="Eligible Revenue" :amount='dataSet.Revenue.eligible'
                          :total='dataSet.Revenue.total'></TaxoCard>
                <TaxoCard title="Eligible CapEx" :amount='dataSet.Capex.eligible'
                          :total='dataSet.Capex.total'></TaxoCard>
                <TaxoCard title="Eligible OpEx" :amount='dataSet.Opex.eligible'
                          :total='dataSet.Opex.total'></TaxoCard>
              </div>
              <div class="col m6">
                <TaxoCard title="Aligned Revenue" :amount='dataSet.Revenue.aligned'
                          :total='dataSet.Revenue.total'></TaxoCard>
                <TaxoCard title="Aligned CapEx" :amount='dataSet.Capex.aligned'
                          :total='dataSet.Capex.total'></TaxoCard>
                <TaxoCard title="Aligned OpEx" :amount='dataSet.Opex.aligned'
                          :total='dataSet.Opex.total'></TaxoCard>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import {EuTaxonomyDataControllerApi, CompanyDataControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";
import TaxoCard from "@/components/ui/TaxoCard";

const euTaxonomyApi = new EuTaxonomyDataControllerApi()
const companyApi = new CompanyDataControllerApi()
const dataStore = new DataStore(euTaxonomyApi.getCompanyAssociatedDataSet)
const companyStore = new DataStore(companyApi.getCompanyById)

export default {
  name: "CompanyEU",
  components: {TaxoCard},
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
        this.dataSet = this.response.data.dataSet
        this.companyInfo = await companyStore.perform(this.response.data.companyId)

      } catch (error) {
        console.error(error)
      }
    },

  }
}
</script>