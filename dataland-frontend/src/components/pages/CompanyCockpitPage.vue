<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="paper-section flex">
    <CompanyInfoSheet :company-id="companyId" @select-company="pushToCompanyCockpit" />
    <div class="card-wrapper">
      <div class="card-grid">
        <FrameworkSummaryPanel
          v-for="framework of Object.values(DataTypeEnum)"
          :company-id="companyId"
          :framework="framework"
          :number-of-provided-reporting-periods="
            aggregatedFrameworkDataSummary?.[framework]?.numberOfProvidedReportingPeriods
          "
          :data-test="`${framework}-summary-panel`"
        />
      </div>
    </div>
  </TheContent>
  <TheFooter />
</template>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import { defineComponent, inject } from "vue";
import PrimeButton from "primevue/button";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import ApiKeyCard from "@/components/resources/apiKey/ApiKeyCard.vue";
import CreateApiKeyCard from "@/components/resources/apiKey/CreateApiKeyCard.vue";
import MessageComponent from "@/components/messages/MessageComponent.vue";
import PrimeDialog from "primevue/dialog";
import PrimeTextarea from "primevue/textarea";
import TheFooter from "@/components/generics/TheFooter.vue";
import CompanyInformationBanner from "@/components/pages/CompanyInformation.vue";
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import CompaniesOnlySearchBar from "@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue";
import { type AggregatedFrameworkDataSummary, type CompanyIdAndName, DataTypeEnum } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import type Keycloak from "keycloak-js";
import FrameworkSummaryPanel from "@/components/resources/companyCockpit/FrameworkSummaryPanel.vue";
import CompanyInfoSheet from "@/components/general/CompanyInfoSheet.vue";

export default defineComponent({
  name: "CompanyCockpitPage",
  inject: {
    injectedUseMobileView: {
      from: "useMobileView",
      default: false,
    },
  },
  computed: {
    useMobileView() {
      return this.injectedUseMobileView;
    },
    DataTypeEnum() {
      return DataTypeEnum;
    },
  },
  components: {
    CompanyInfoSheet,
    FrameworkSummaryPanel,
    MarginWrapper,
    FrameworkDataSearchBar,
    CompanyInformationBanner,
    AuthenticationWrapper,
    TheContent,
    TheHeader,
    MiddleCenterDiv,
    BackButton,
    PrimeButton,
    PrimeDialog,
    ApiKeyCard,
    CreateApiKeyCard,
    MessageComponent,
    PrimeTextarea,
    TheFooter,
    CompaniesOnlySearchBar,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  props: {
    companyId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      aggregatedFrameworkDataSummary: undefined as
        | { [key in DataTypeEnum]: AggregatedFrameworkDataSummary }
        | undefined,
    };
  },
  mounted() {
    this.getAggregatedFrameworkDataSummary();
  },
  methods: {
    assertDefined,
    /**
     * Handles the "search-confirmed" event of the search bar by visiting the search page with the query param set to
     * the search term provided by the event.
     * @param searchTerm The search term provided by the "search-confirmed" event of the search bar
     */
    async handleSearchConfirm(searchTerm: string) {
      await this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: searchTerm },
      });
    },
    /**
     * Retrieves the aggregated framework data summary
     */
    async getAggregatedFrameworkDataSummary() {
      const companyDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)(),
      ).getCompanyDataControllerApi();
      this.aggregatedFrameworkDataSummary = (
        await companyDataControllerApi.getAggregatedFrameworkDataSummary(this.companyId)
      ).data;
    },

    /**
     * Executes a router push to upload overview page of the given company
     * @param selectedCompany the company selected through the search bar
     */
    async pushToCompanyCockpit(selectedCompany: CompanyIdAndName) {
      await this.$router.push(`/companies/${selectedCompany.companyId}`);
    },

    handleFetchedCompanyInformation() {},
  },
});
</script>

<style lang="scss" scoped>
.card-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  padding-top: 40px;
  padding-bottom: 40px;
  @media only screen and (max-width: $small) {
    padding: 24px 17px;
  }
}

.card-grid {
  width: 70%;
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  justify-content: center;
  @media only screen and (max-width: $small) {
    width: 100%;
  }
}
</style>
