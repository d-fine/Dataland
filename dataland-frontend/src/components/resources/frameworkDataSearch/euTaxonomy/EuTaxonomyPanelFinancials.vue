<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading Eu Taxonomy Data..</p>
    <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="dataSet && !waitingForData">
    <ShowReportsBanner
      v-if="dataSet.referencedReports !== undefined && Object.keys(dataSet.referencedReports).length > 0"
      :reports="dataSet.referencedReports"
    />
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
        <div class="card-section-title">
          <span class="font-medium text-xl font-semibold">Exposures for {{ getSectionHeading(fsType) }}</span>
          <span class="pl-2 font-italic">In percentage of the total assets</span>
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
        <div class="col-6">
          <TaxoCard
            :name="`taxonomyNonEligibleActivity${fsType}`"
            title="Taxonomy-non-eligible economic activity"
            :percent="dataSet.eligibilityKpis[fsType].taxonomyNonEligibleActivity?.value"
          />
        </div>
        <template v-if="fsType === 'CreditInstitution'">
          <div class="card-section-title">
            <span class="font-medium text-xl font-semibold">Credit Institution KPIs</span>
            <span class="pl-2 font-italic">In percentage of the total assets</span>
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
          <div class="col-6">
            <TaxoCard
              name="greenAssetRatioCreditInstitution"
              title="Green asset ratio"
              :percent="dataSet.creditInstitutionKpis.greenAssetRatio?.value"
            />
          </div>
        </template>
        <template v-if="fsType === 'InsuranceOrReinsurance'">
          <div class="card-section-title">
            <span class="font-medium text-xl font-semibold">Insurance and Reinsurance KPIs</span>
            <span class="pl-2 font-italic">In percentage of the total assets</span>
          </div>
          <div class="col-6">
            <TaxoCard
              name="taxonomyEligibleNonLifeInsuranceActivities"
              title="Taxonomy-eligible non-life insurance economic activities"
              :percent="dataSet.insuranceKpis.taxonomyEligibleNonLifeInsuranceActivities?.value"
            />
          </div>
        </template>
        <template v-if="fsType === 'InvestmentFirm'">
          <div class="card-section-title">
            <span class="font-medium text-xl font-semibold">Investment Firm KPIs</span>
            <span class="pl-2 font-italic">In percentage of the total assets</span>
          </div>
          <div class="col-6">
            <TaxoCard
              name="greenAssetRatioInvestmentFirm"
              title="Green asset ratio"
              :percent="dataSet.investmentFirmKpis.greenAssetRatio?.value"
            />
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { EuTaxonomyDataForFinancials } from "@clients/backend";
import TaxoCard from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxoCard.vue";
import TaxoInfoCard from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxoInfoCard.vue";
import ShowReportsBanner from "@/components/resources/frameworkDataSearch/ShowReportsBanner.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "EuTaxonomyPanelFinancials",
  components: { TaxoCard, TaxoInfoCard, ShowReportsBanner },
  data() {
    return {
      dataSet: null as EuTaxonomyDataForFinancials | null | undefined,
    };
  },
  props: {
    dataID: {
      type: String,
      default: "loading",
    },
  },
  mounted() {
    void this.getCompanyEuDataset();
  },
  watch: {
    dataID() {
      void this.getCompanyEuDataset();
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      waitingForData: true,
    };
  },
  methods: {
    /**
     * Uses the Dataland API to retrieve the eutaxonomy-financials dataset identified by this components
     * dataID and stores it in this components dataSet attribute
     */
    async getCompanyEuDataset() {
      try {
        this.waitingForData = true;
        if (this.dataID != "loading") {
          const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
          ).getEuTaxonomyDataForFinancialsControllerApi();
          const companyAssociatedData =
            await euTaxonomyDataForFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForFinancials(
              assertDefined(this.dataID)
            );
          this.dataSet = companyAssociatedData.data.data;
          this.waitingForData = false;
        } else if (this.dataID == "loading") {
          console.log("Loading Loading Loading");
        }
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Converts the type of financial company to the section heading
     *
     * @param type the type of financial company to convert
     * @returns the section heading of the specified fs type
     */
    getSectionHeading(type: string): string {
      const mapping: { [key: string]: string } = {
        CreditInstitution: "Credit Institution",
        AssetManagement: "Asset Management",
        InsuranceOrReinsurance: "Insurance and Reinsurance",
        InvestmentFirm: "Investment Firm",
      };
      return mapping[type];
    },
  },
});
</script>
