<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading Eu Taxonomy Data...</p>
    <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="dataSet && !waitingForData">
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
      <div class="card-section-title">
        <span class="font-medium text-xl font-semibold">Revenue</span>
      </div>
      <div class="col-6">
        <TaxoCard
          title="Eligible Revenue"
          :percent="dataSet.revenue?.eligiblePercentage?.value"
          :total="dataSet.revenue?.totalAmount?.value"
        ></TaxoCard>
      </div>
      <div class="col-6">
        <TaxoCard
          title="Aligned Revenue"
          :percent="dataSet.revenue?.alignedPercentage?.value"
          :total="dataSet.revenue?.totalAmount?.value"
        ></TaxoCard>
      </div>
    </div>
    <div class="grid">
      <div class="card-section-title">
        <span class="font-medium text-xl font-semibold">CapEx</span>
      </div>
      <div class="col-6">
        <TaxoCard
          title="Eligible CapEx"
          :percent="dataSet.capex?.eligiblePercentage?.value"
          :total="dataSet.capex?.totalAmount?.value"
        />
      </div>
      <div class="col-6">
        <TaxoCard
          title="Aligned CapEx"
          :percent="dataSet.capex?.alignedPercentage?.value"
          :total="dataSet.capex?.totalAmount?.value"
        />
      </div>
    </div>
    <div class="grid">
      <div class="card-section-title">
        <span class="font-medium text-xl font-semibold">OpEx</span>
      </div>
      <div class="col-6">
        <TaxoCard
          title="Eligible OpEx"
          :percent="dataSet.opex?.eligiblePercentage?.value"
          :total="dataSet.opex?.totalAmount?.value"
        />
      </div>
      <div class="col-6">
        <TaxoCard
          title="Aligned OpEx"
          :percent="dataSet.opex?.alignedPercentage?.value"
          :total="dataSet.opex?.totalAmount?.value"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import TaxoCard from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxoCard.vue";
import TaxoInfoCard from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxoInfoCard.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "EuTaxonomyPanelNonFinancials",
  components: { TaxoCard, TaxoInfoCard },
  data() {
    return {
      kpiDataObjects: null as EuTaxonomyDataForNonFinancials | null | undefined,
      waitingForData: true,
    };
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
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
  methods: {
    /**
     * Uses the Dataland API to retrieve the eutaxonomy-non-financials dataset identified by this components
     * dataID and stores it in this components dataSet attribute
     */
    async getCompanyEuDataset() {
      try {
        this.waitingForData = true;
        if (this.dataID != "loading") {
          const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
          ).getEuTaxonomyDataForNonFinancialsControllerApi();
          const companyAssociatedData =
            await euTaxonomyDataForNonFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(
              assertDefined(this.dataID)
            );
          this.dataSet = companyAssociatedData.data.data;
          this.waitingForData = false;
        }
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
