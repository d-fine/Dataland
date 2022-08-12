<template>
  <div v-if="dataSet">
    <div class="grid">
      <div class="col-6">
        <TaxoInfoCard
          title="NFRD required"
          :value="dataSet['reportingObligation']"
          tooltipText="The NFRD (Non financial disclosure directive) applies to companies with more than 500 employees with a > €20M balance or > €40M net turnover."
        ></TaxoInfoCard>
      </div>
      <div class="col-6">
        <TaxoInfoCard
          title="Level of Assurance"
          :value="dataSet['attestation']"
          tooltipText="The Level of Assurance specifies the confidence level of the data reported.
                  Reasonable assurance:  relatively high degree of comfort that the subject matter is not materially misstated.
                  Limited assurance: moderate level of comfort that the subject matter is not materially misstated.
                  None: low level of comfort that the subject matter is not materially misstated."
        ></TaxoInfoCard>
      </div>
      <div class="col-12 text-left pb-0">
        <h3>Exposure</h3>
      </div>
      <div class="col-6">
        <TaxoCard
            taxonomyType="Taxonomy-eligible economic activity"
            taxonomy-kind=""
            :percent="dataSet['Taxonomy Eligible Activity']"
            :total="2"
        />
      </div>
      <div class="col-6">
        <TaxoCard
            taxonomyType="Derivatives"
            taxonomy-kind=""
            :percent="dataSet['Derivatives']"
            :total="2"
        />
      </div>
      <div class="col-6">
        <TaxoCard
            taxonomyType="Banks and issuers"
            taxonomy-kind=""
            :percent="dataSet['Banks and Issuers']"
            :total="2"
        />
      </div>
      <div class="col-6">
        <TaxoCard
            taxonomyType="Non-NFRD"
            taxonomy-kind=""
            :percent="dataSet['Investment non Nfrd']"
            :total="2"
        />
      </div>
      <div class="col-12 text-left pb-0" v-if="dataSet['Financial Services Type'] != 'AssetManagement'">
        <h3>{{ dataSet['Financial Services Type'] }} KPIs</h3>
      </div>
      <div v-if="dataSet['Financial Services Type'] === 'CreditInstitution'">
        <div class="col-6">
          <TaxoCard
              taxonomyType="Trading portfolio & on demand interbank loans"
              taxonomy-kind=""
              :percent="dataSet['Trading Portfolio and Interbank Loans']"
              :total="2"
          />
        </div>
        <div class="col-6">
          <TaxoCard
              taxonomyType="Trading portfolio"
              taxonomy-kind=""
              :percent="dataSet['Trading Portfolio']"
              :total="2"
          />
        </div>
        <div class="col-6">
          <TaxoCard
              taxonomyType="On demand interbank loans"
              taxonomy-kind=""
              :percent="dataSet['Interbank Loans']"
              :total="2"
          />
        </div>
      </div>
      <div class="col-6" v-if="dataSet[`Financial Services Type`] === 'InsuranceOrReinsurance'">
        <TaxoCard
            taxonomyType="Taxonomy-eligible non-life insurance economic activities"
            taxonomy-kind=""
            :percent="dataSet['Taxonomy Eligible non Life Insurance Activities']"
            :total="2"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ApiClientProvider } from "@/services/ApiClients";
import TaxoCard from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxoCard";
import TaxoInfoCard from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxoInfoCard";

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
