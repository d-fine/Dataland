<template>
  <div v-if="response">
    <div class="grid">
      <div class="col-6">
        <TaxoInfoCard title="NFRD required" :value="dataSet['Reporting Obligation']"
                  tooltipText='The NFRD (Non financial disclosure directive) applies to companies with more than 500 employees with a > €20M balance or > €40M net turnover.'></TaxoInfoCard>
      </div>
      <div class="col-6">
        <TaxoInfoCard title="Level of Assurance" :value="dataSet['Attestation']"
                      tooltipText='The Level of Assurance specifies the confidence level of the data reported.
                  Reasonable assurance:  relatively high degree of comfort that the subject matter is not materially misstated.
                  Limited assurance: moderate level of comfort that the subject matter is not materially misstated.
                  None: low level of comfort that the subject matter is not materially misstated.'></TaxoInfoCard>
      </div>
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
import {EuTaxonomyDataControllerApi} from "../../../../build/clients/backend/api";
import {ApiWrapper} from "@/services/ApiWrapper"
import TaxoCard from "@/components/resources/taxonomy/TaxoCard";
import TaxoInfoCard from "@/components/resources/taxonomy/TaxoInfoCard";

const euTaxonomyDataControllerApi = new EuTaxonomyDataControllerApi()
const getCompanyAssociatedDataWrapper = new ApiWrapper(euTaxonomyDataControllerApi.getCompanyAssociatedData)

export default {
  name: "TaxonomyPanel",
  components: {TaxoCard, TaxoInfoCard},
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
  watch: {
    dataID(){
      this.getCompanyEUDataset()
    }
  },
  methods: {
    async getCompanyEUDataset() {
      try {
        this.response = await getCompanyAssociatedDataWrapper.perform(this.dataID)
        this.dataSet = this.response.data.data

      } catch (error) {
        console.error(error)
      }
    },

  }
}
</script>