<template>
  <div v-if="response">
    <div class="grid">
      <div class="col-6">
        <TaxoInfoCard
          title="NFRD required"
          :value="dataSet['Reporting Obligation']"
          tooltipText="The NFRD (Non financial disclosure directive) applies to companies with more than 500 employees with a > €20M balance or > €40M net turnover."
        ></TaxoInfoCard>
      </div>
      <div class="col-6">
        <TaxoInfoCard
          title="Level of Assurance"
          :value="dataSet['Attestation']"
          tooltipText="The Level of Assurance specifies the confidence level of the data reported.
                  Reasonable assurance:  relatively high degree of comfort that the subject matter is not materially misstated.
                  Limited assurance: moderate level of comfort that the subject matter is not materially misstated.
                  None: low level of comfort that the subject matter is not materially misstated."
        ></TaxoInfoCard>
      </div>
      <div class="col-12 text-left pb-0">
        <h3>Revenue</h3>
      </div>
      <div class="col-6">
        <TaxoCard
          taxonomyKind="Revenue"
          taxonomyType="eligible"
          :percent="dataSet.Revenue.eligiblePercentage"
          :total="dataSet.Revenue.totalAmount"
        ></TaxoCard>
      </div>
      <div class="col-6">
        <TaxoCard
          taxonomyKind="Revenue"
          taxonomyType="aligned"
          :percent="dataSet.Revenue.alignedPercentage"
          :total="dataSet.Revenue.totalAmount"
        ></TaxoCard>
      </div>
    </div>
    <div class="grid">
      <div class="col-12 text-left pb-0">
        <h3>CapEx</h3>
      </div>
      <div class="col-6">
        <TaxoCard
          taxonomyKind="CapEx"
          taxonomyType="eligible"
          :percent="dataSet.Capex.eligiblePercentage"
          :total="dataSet.Capex.totalAmount"
        ></TaxoCard>
      </div>
      <div class="col-6">
        <TaxoCard
          taxonomyKind="CapEx"
          taxonomyType="aligned"
          :percent="dataSet.Capex.alignedPercentage"
          :total="dataSet.Capex.totalAmount"
        ></TaxoCard>
      </div>
    </div>
    <div class="grid">
      <div class="col-12 text-left pb-0">
        <h3>OpEx</h3>
      </div>
      <div class="col-6">
        <TaxoCard
          taxonomyKind="OpEx"
          taxonomyType="eligible"
          :percent="dataSet.Opex.eligiblePercentage"
          :total="dataSet.Opex.totalAmount"
        ></TaxoCard>
      </div>
      <div class="col-6">
        <TaxoCard
          taxonomyKind="OpEx"
          taxonomyType="aligned"
          :percent="dataSet.Opex.alignedPercentage"
          :total="dataSet.Opex.totalAmount"
        ></TaxoCard>
      </div>
    </div>
  </div>
</template>

<script>
import { ApiClientProvider } from "@/services/ApiClients";
import TaxoCard from "@/components/resources/taxonomy/TaxoCard";
import TaxoInfoCard from "@/components/resources/taxonomy/TaxoInfoCard";

export default {
  name: "TaxonomyPanel",
  components: { TaxoCard, TaxoInfoCard },
  data() {
    return {
      response: null,
      dataSet: null,
      companyInfo: null,
    };
  },
  props: {
    dataID: String,
  },
  created() {
    this.getCompanyEUDataset();
  },
  watch: {
    dataID() {
      this.getCompanyEUDataset();
    },
  },
  inject: ["getKeycloakInitPromise"],
  methods: {
    async getCompanyEUDataset() {
      try {
        const euTaxonomyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise(),
        ).getEuTaxonomyDataControllerApi();
        this.response = await euTaxonomyDataControllerApi.getCompanyAssociatedData(this.dataID);
        this.dataSet = this.response.data.data;
      } catch (error) {
        console.error(error);
      }
    },
  },
};
</script>
