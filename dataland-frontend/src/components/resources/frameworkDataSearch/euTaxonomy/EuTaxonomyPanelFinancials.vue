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
          :value="dataSet.assurance?.assurance"
          tooltipText="The Level of Assurance specifies the confidence level of the data reported.
                  Reasonable assurance:  relatively high degree of comfort that the subject matter is not materially misstated.
                  Limited assurance: moderate level of comfort that the subject matter is not materially misstated.
                  None: low level of comfort that the subject matter is not materially misstated."
        />
      </div>
      <template v-for="fsType in dataSet.financialServicesTypes" :key="fsType">
        <div class="col-12 text-left pb-3">
          <span class="font-medium text-xl">Exposures for {{ getSectionHeading(fsType) }}</span>
          <span class="pl-2 font-italic text-gray-100">In percentage of the total assets</span>
        </div>
        <div class="col-6">
          <TaxoCard
            :name="`taxonomyEligibleActivity${fsType}`"
            title="Taxonomy-eligible economic activity"
            :percent="dataSet.eligibilityKpis[fsType].taxonomyEligibleActivity?.value"
          />
        </div>
        <div class="col-6">
          <TaxoCard
            :name="`derivatives${fsType}`"
            title="Derivatives"
            taxonomy-kind=""
            :percent="dataSet.eligibilityKpis[fsType].derivatives?.value"
          />
        </div>
        <div class="col-6">
          <TaxoCard
            :name="`banksAndIssuers${fsType}`"
            title="Banks and issuers"
            :percent="dataSet.eligibilityKpis[fsType].banksAndIssuers?.value"
          />
        </div>
        <div class="col-6">
          <TaxoCard
            :name="`investmentNonNfrd${fsType}`"
            title="Non-NFRD"
            :percent="dataSet.eligibilityKpis[fsType].investmentNonNfrd?.value"
          />
        </div>
        <template v-if="fsType === 'CreditInstitution'">
          <div class="col-12 text-left pb-3">
            <span class="font-medium text-xl">Credit Institution KPIs</span>
            <span class="pl-2 font-italic text-gray-100">In percentage of the total assets</span>
          </div>
          <div
            class="col-6"
            v-if="
              dataSet.creditInstitutionKpis.tradingPortfolioAndInterbankLoans ||
              (!dataSet.creditInstitutionKpis.tradingPortfolio && !dataSet.creditInstitutionKpis.interbankLoans)
            "
          >
            <TaxoCard
              title="Trading portfolio & on demand interbank loans"
              name="tradingPortfolioAndOnDemandInterbankLoans"
              taxonomy-kind=""
              :percent="dataSet.creditInstitutionKpis.tradingPortfolioAndInterbankLoans?.value"
            />
          </div>
          <div
            class="col-6"
            v-if="
              dataSet.creditInstitutionKpis.tradingPortfolio ||
              !dataSet.creditInstitutionKpis.tradingPortfolioAndInterbankLoans
            "
          >
            <TaxoCard
              name="tradingPortfolio"
              title="Trading portfolio"
              :percent="dataSet.creditInstitutionKpis.tradingPortfolio?.value"
            />
          </div>
          <div
            class="col-6"
            v-if="
              dataSet.creditInstitutionKpis.interbankLoans ||
              !dataSet.creditInstitutionKpis.tradingPortfolioAndInterbankLoans
            "
          >
            <TaxoCard
              name="onDemandInterbankLoans"
              title="On demand interbank loans"
              :percent="dataSet.creditInstitutionKpis.interbankLoans?.value"
            />
          </div>
        </template>
        <template v-if="fsType === 'InsuranceOrReinsurance'">
          <div class="col-12 text-left pb-0">
            <span class="font-medium text-xl">Insurance and Reinsurance KPIs</span>
            <span class="pl-2 font-italic text-gray-100">In percentage of the total assets</span>
          </div>
          <div class="col-6">
            <TaxoCard
              name="taxonomyEligibleNonLifeInsuranceActivities"
              title="Taxonomy-eligible non-life insurance economic activities"
              :percent="dataSet.insuranceKpis.taxonomyEligibleNonLifeInsuranceActivities?.value"
            />
          </div>
        </template>
      </template>
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
    getSectionHeading(type) {
      const mapping = {
        CreditInstitution: "Credit Institution",
        AssetManagement: "Asset Management",
        InsuranceOrReinsurance: "Insurance and Reinsurance",
      };
      return mapping[type];
    },
  },
};
</script>
