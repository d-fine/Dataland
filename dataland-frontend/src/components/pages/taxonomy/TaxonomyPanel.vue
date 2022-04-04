<template>
  <div v-if="response">
    <div class="grid">
      <div class="col-12 text-left pb-0">
        <h3>Revenue</h3>
      </div>
      <div class="col-6">
        <TaxoCard title="Eligible Revenue" :amount='dataSet.Revenue.eligible'
                  :total='dataSet.Revenue.total'></TaxoCard>
      </div>
      <div class="col-6">
        <TaxoCard title="Aligned Revenue" :amount='dataSet.Revenue.aligned'
                  :total='dataSet.Revenue.total'></TaxoCard>
      </div>

    </div>
    <div class="grid">
      <div class="col-12 text-left pb-0">
        <h3>CapEx</h3>
      </div>
      <div class="col-6">
        <TaxoCard title="Eligible CapEx" :amount='dataSet.Capex.eligible'
                  :total='dataSet.Capex.total'></TaxoCard>
      </div>
      <div class="col-6">
        <TaxoCard title="Aligned CapEx" :amount='dataSet.Capex.aligned'
                  :total='dataSet.Capex.total'></TaxoCard>
      </div>
    </div>
    <div class="grid">
      <div class="col-12 text-left pb-0">
        <h3>OpEx</h3>
      </div>
      <div class="col-6">
        <TaxoCard title="Eligible OpEx" :amount='dataSet.Opex.eligible'
                  :total='dataSet.Opex.total'></TaxoCard>
      </div>
      <div class="col-6">
        <TaxoCard title="Aligned OpEx" :amount='dataSet.Opex.aligned'
                  :total='dataSet.Opex.total'></TaxoCard>
      </div>
    </div>
  </div>
</template>

<script>
import {EuTaxonomyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import TaxoCard from "@/components/pages/taxonomy/TaxoCard";

const euTaxonomyApi = new EuTaxonomyDataControllerApi()
const dataStore = new DataStore(euTaxonomyApi.getCompanyAssociatedDataSet)

export default {
  name: "TaxonomyPanel",
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
      default: 1,
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

      } catch (error) {
        console.error(error)
      }
    },

  }
}
</script>