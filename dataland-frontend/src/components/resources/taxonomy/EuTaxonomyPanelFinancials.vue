<template>
  <div v-if="dataSet">
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
      <div class="col-6">
        <TaxoInfoCard
            title="Financial Services Type"
            :value="dataSet['Financial Services Type']"
            tooltipText="To Do"
        ></TaxoInfoCard>
      </div>
      <div class="col-12 text-left pb-0">
        <h3>Taxonomy Eligible Activity</h3>
      </div>
      <div class="col-6">
        <TaxoCard
          taxonomyKind="Taxonomy Eligible Activity"
          percent="3"
          total="5"
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
  name: "EuTaxonomyPanelFinancials",
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
        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          this.getKeycloakPromise()
        ).getEuTaxonomyDataForFinancialsControllerApi();
        const companyAssociatedData = await euTaxonomyDataForFinancialsControllerApi.getCompanyAssociatedData1(
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
