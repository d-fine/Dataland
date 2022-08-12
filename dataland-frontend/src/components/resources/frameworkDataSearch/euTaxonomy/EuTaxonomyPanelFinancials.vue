<template>
  <div v-if="dataSet">
    <div class="grid">
      <div class="col-6">
        <TaxoInfoCard
          title="NFRD required"
          :value="dataSet.reportingObligation"
          tooltipText="The NFRD (Non financial disclosure directive) applies to companies with more than 500 employees with a > €20M balance or > €40M net turnover."
        />
      </div>
      <div class="col-6">
        <TaxoInfoCard
          title="Level of Assurance"
          :value="dataSet.attestation"
          tooltipText="The Level of Assurance specifies the confidence level of the data reported.
                  Reasonable assurance:  relatively high degree of comfort that the subject matter is not materially misstated.
                  Limited assurance: moderate level of comfort that the subject matter is not materially misstated.
                  None: low level of comfort that the subject matter is not materially misstated."
        />
      </div>
      <div class="col-12 text-left pb-0">
        <h3>Exposure</h3>
      </div>
      <div class="col-6">
        <TaxoCard
          title="Taxonomy-eligible economic activity"
          :percent="dataSet.eligibilityKpis.taxonomyEligibleActivity"
        />
      </div>
      <div class="col-6">
        <TaxoCard title="Derivatives" taxonomy-kind="" :percent="dataSet['Derivatives']" :total="2" />
      </div>
      <div class="col-6">
        <TaxoCard
          title="Banks and issuers"
          :percent="dataSet.eligibilityKpis.banksAndIssuers"
        />
      </div>
      <div class="col-6">
        <TaxoCard title="Non-NFRD" taxonomy-kind="" :percent="dataSet['Investment non Nfrd']" :total="2" />
      </div>
      <div class="col-12 text-left pb-0" v-if="dataSet.financialServicesType != 'AssetManagement'">
        <h3>{{ dataSet.financialServicesType }} KPIs</h3>
      </div>
      <div class="col-12" v-if="dataSet.financialServicesType === 'CreditInstitution'">
        <div class="col-6" v-if="!dataSet.creditInstitutionKpis.tradingPortfolio && !dataSet.creditInstitutionKpis.interbankLoans">
          <TaxoCard
            title="Trading portfolio & on demand interbank loans"
            taxonomy-kind=""
            :percent="dataSet.creditInstitutionKpis.tradingPortfolioAndInterbankLoans"
          />
        </div>
        <div class="col-6" v-if="dataSet.creditInstitutionKpis.tradingPortfolio">
          <TaxoCard
            title="Trading portfolio"
            :percent="dataSet.creditInstitutionKpis.tradingPortfolio"
          />
        </div>
        <div class="col-6" v-if="dataSet.creditInstitutionKpis.interbankLoans">
          <TaxoCard
            title="On demand interbank loans"
            :percent="dataSet.creditInstitutionKpis.interbankLoans"
          />
        </div>
      </div>
      <div class="col-6" v-if="dataSet.financialServicesType === 'InsuranceOrReinsurance'">
        <TaxoCard
          title="Taxonomy-eligible non-life insurance economic activities"
          :percent="dataSet.insuranceKpis.taxonomyEligibleNonLifeInsuranceActivities"
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
