<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="paper-section flex">
    <CompanyInfoSheet :company-id="companyId" @fetched-company-information="getCompanyName" />
    <div class="card flex justify-content-center">
      <Button type="button" icon="pi pi-ellipsis-v" @click="toggle" aria-haspopup="true" aria-controls="overlay_menu" />
      <Menu ref="menu" id="overlay_menu" :model="menuItems" :popup="true" />
    </div>
    <div class="card-wrapper">
      <div class="card-grid">
        <ClaimOwnershipPanel :company-name="companyName" />
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
  <TheFooter />
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import TheFooter from "@/components/generics/TheFooter.vue";
import { type AggregatedFrameworkDataSummary, CompanyInformation, type DataTypeEnum } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import type Keycloak from "keycloak-js";
import FrameworkSummaryPanel from "@/components/resources/companyCockpit/FrameworkSummaryPanel.vue";
import CompanyInfoSheet from "@/components/general/CompanyInfoSheet.vue";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import ClaimOwnershipPanel from "@/components/resources/companyCockpit/ClaimOwnershipPanel.vue";
import Menu from "primevue/menu";
import Button from "primevue/button";

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
    TheHeader,
    TheFooter,
    Menu,
    Button,
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
      ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE,
      companyName: "wait",
      menuItems: [
        {
          label: "Manage Company Details",
          command: () => {
            console.log("I dont know where to route this ? #TODO TODO");
          },
        },
        {
          label: "Claim Company Ownership",
          command: () => {
            console.log("clik- this will lead to the known dialog");
          },
        },
      ],
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
    toggle(event: Event) {
      this.$refs.menu.toggle(event);
    },
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
