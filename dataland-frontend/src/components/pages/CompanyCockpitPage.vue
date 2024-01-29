<template>
  <TheHeader v-if="!useMobileView"/>
  <TheContent class="paper-section flex">
    <CompanyInfoSheet :company-id="companyId" @fetched-company-information="getCompanyName"
                      @fetched-data-owner-information="getDataOwnerInformation"
                      @claim-data-owner-ship="openDialog"/>
    <div class="card-wrapper">
      <div class="card-grid">

        <ClaimOwnershipPanel v-if="!isUserDataOwner" :company-name="companyName" @toggle-dialog="toggleDialog"/>
        <ClaimOwnershipDialog v-if="!isUserDataOwner" :dialog-is-open="dialogIsOpen" :company-name="companyName"
                              :company-id="companyId"
                              @toggle-dialog="toggleDialog"/>
        <FrameworkSummaryPanel
            v-for="framework of ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE"
            :key="framework"
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
  <TheFooter :is-light-version="true" :sections="footerContent"/>
</template>

<script lang="ts">
import {defineComponent, inject} from "vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import {type AggregatedFrameworkDataSummary, CompanyInformation, type DataTypeEnum} from "@clients/backend";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import TheFooter from "@/components/generics/TheNewFooter.vue";
import contentData from "@/assets/content.json";
import type {Content, Page} from "@/types/ContentTypes";
import type Keycloak from "keycloak-js";
import FrameworkSummaryPanel from "@/components/resources/companyCockpit/FrameworkSummaryPanel.vue";
import CompanyInfoSheet from "@/components/general/CompanyInfoSheet.vue";
import {ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE} from "@/utils/Constants";
import ClaimOwnershipPanel from "@/components/resources/companyCockpit/ClaimOwnershipPanel.vue";
import ClaimOwnershipDialog from "@/components/resources/companyCockpit/ClaimOwnershipDialog.vue";

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
  },
  watch: {
    async companyId(newCompanyId, oldCompanyId) {
      if (newCompanyId !== oldCompanyId) {
        try {
          await this.getAggregatedFrameworkDataSummary();
        } catch (error) {
          console.error("Error fetching data for new company:", error);
        }
      }
    },
  },
  components: {
    ClaimOwnershipPanel,
    CompanyInfoSheet,
    FrameworkSummaryPanel,
    TheContent,
    ClaimOwnershipDialog,
    TheHeader,
    TheFooter,
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
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === "/");
    const footerContent = footerPage?.sections;
    return {
      aggregatedFrameworkDataSummary: undefined as
          | { [key in DataTypeEnum]: AggregatedFrameworkDataSummary }
          | undefined,
      ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE,
      companyName: "wait",
      dialogIsOpen: false,
      isUserDataOwner: false,
      footerContent,
    };
  },
  mounted() {
    void this.getAggregatedFrameworkDataSummary();
  },
  methods: {
    /**
     * Retrieves the aggregated framework data summary
     */
    async getAggregatedFrameworkDataSummary(): Promise<void> {
      const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients
          .companyDataController;
      this.aggregatedFrameworkDataSummary = (
          await companyDataControllerApi.getAggregatedFrameworkDataSummary(this.companyId)
      ).data as { [key in DataTypeEnum]: AggregatedFrameworkDataSummary } | undefined;
    },
    getCompanyName(companyInfo: CompanyInformation) {
      this.companyName = companyInfo.companyName;
    },
    getDataOwnerInformation(isUserDataOwner: boolean) {
      this.isUserDataOwner = isUserDataOwner;
    },
    toggleDialog() {
      this.dialogIsOpen = !this.dialogIsOpen;
    },
    openDialog() {
      this.dialogIsOpen = true;
    }
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
  width: 80%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  flex-wrap: wrap;
  justify-content: space-between;
  @media only screen and (max-width: $medium) {
    grid-template-columns: repeat(2, 1fr);
  }
  @media only screen and (max-width: $small) {
    width: 100%;
    grid-template-columns: repeat(1, 1fr);
  }
}
</style>
