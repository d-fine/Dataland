<template>
  <div v-if="dataSet">
    <div class="grid">
      <div class="col-6">
        <TaxoInfoCard
          title="NFRD required"
          :value="dataSet['reportingObligation']"
          tooltipText="The NFRD (Non financial disclosure directive) applies to companies with more than 500 employees with a > €20M balance or > €40M net turnover."
        />
      </div>
      <div class="col-6">
        <TaxoInfoCard
          title="Level of Assurance"
          :value="dataSet['attestation']"
          tooltipText="The Level of Assurance specifies the confidence level of the data reported.
                  Reasonable assurance:  relatively high degree of comfort that the subject matter is not materially misstated.
                  Limited assurance: moderate level of comfort that the subject matter is not materially misstated.
                  None: low level of comfort that the subject matter is not materially misstated."
        />
      </div>
      <div class="col-12 text-left pb-0">
        <h3>Revenue</h3>
      </div>
      <div class="col-6">
        <TaxoCard
          title="Eligible Revenue"
          :percent="dataSet.revenue.eligiblePercentage"
          :total="dataSet.revenue.totalAmount"
        ></TaxoCard>
      </div>
      <div class="col-6">
        <TaxoCard
          title="Aligned Revenue"
          :percent="dataSet.revenue.alignedPercentage"
          :total="dataSet.revenue.totalAmount"
        ></TaxoCard>
      </div>
    </div>
    <div class="grid">
      <div class="col-12 text-left pb-0">
        <h3>CapEx</h3>
      </div>
      <div class="col-6">
        <TaxoCard
          title="Eligible CapEx"
          :percent="dataSet.capex.eligiblePercentage"
          :total="dataSet.capex.totalAmount"
        />
      </div>
      <div class="col-6">
        <TaxoCard title="Aligned CapEx" :percent="dataSet.capex.alignedPercentage" :total="dataSet.capex.totalAmount" />
      </div>
    </div>
    <div class="grid">
      <div class="col-12 text-left pb-0">
        <h3>OpEx</h3>
      </div>
      <div class="col-6">
        <TaxoCard title="Eligible OpEx" :percent="dataSet.opex.eligiblePercentage" :total="dataSet.opex.totalAmount" />
      </div>
      <div class="col-6">
        <TaxoCard title="Aligned OpEx" :percent="dataSet.opex.alignedPercentage" :total="dataSet.opex.totalAmount" />
      </div>
    </div>
  </div>
</template>

<script>
import { ApiClientProvider } from "@/services/ApiClients";
import TaxoCard from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxoCard";
import TaxoInfoCard from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxoInfoCard";

export default {
  name: "EuTaxonomyPanelNonFinancials",
  components: { TaxoCard, TaxoInfoCard },
  data() {
    return {
      dataSet: null,
    };
  },
  props: {
    dataID: String,
  },
  mounted() {
    this.getCompanyEUDataset();
  },
  watch: {
    dataID() {
      this.getCompanyEUDataset();
    },
  },
  inject: ["getKeycloakPromise"],
  methods: {
    async getCompanyEUDataset() {
      try {
        const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
          this.getKeycloakPromise()
        ).getEuTaxonomyDataForNonFinancialsControllerApi();
        const companyAssociatedData = await euTaxonomyDataForNonFinancialsControllerApi.getCompanyAssociatedData(
          this.dataID
        );
        this.dataSet = companyAssociatedData.data.data;
      } catch (error) {
        console.error(error);
      }
    },
  },
};
</script>
