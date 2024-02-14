<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="paper-section flex">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="true" />
    <div class="card-wrapper">
      <div class="card-grid">
        <ClaimOwnershipPanel v-if="isClaimPanelVisible" :company-id="companyId" />

        <FrameworkSummaryPanel
          v-for="framework of ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE"
          :key="framework"
          :is-user-allowed-to-upload="isUserAllowedToUpload"
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
  <TheFooter :is-light-version="true" :sections="footerContent" />
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import { type AggregatedFrameworkDataSummary, type DataTypeEnum } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import TheFooter from "@/components/generics/TheNewFooter.vue";
import contentData from "@/assets/content.json";
import type { Content, Page } from "@/types/ContentTypes";
import type Keycloak from "keycloak-js";
import FrameworkSummaryPanel from "@/components/resources/companyCockpit/FrameworkSummaryPanel.vue";
import CompanyInfoSheet from "@/components/general/CompanyInfoSheet.vue";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import ClaimOwnershipPanel from "@/components/resources/companyCockpit/ClaimOwnershipPanel.vue";
import { checkIfUserHasRole, KEYCLOAK_ROLE_UPLOADER } from "@/utils/KeycloakUtils";
import { hasCompanyAtLeastOneDataOwner, isUserDataOwnerForCompany } from "@/utils/DataOwnerUtils";
import { isCompanyIdValid } from "@/utils/ValidationsUtils";

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
    isClaimPanelVisible() {
      return (
        !this.isUserDataOwner && this.authenticated && isCompanyIdValid(this.companyId) && !this.hasCompanyDataOwner
      );
    },
  },
  watch: {
    async companyId(newCompanyId, oldCompanyId) {
      if (newCompanyId !== oldCompanyId) {
        try {
          await this.getAggregatedFrameworkDataSummary();
          await this.setUploaderRightsForUser();
          this.hasCompanyDataOwner = await hasCompanyAtLeastOneDataOwner(
            newCompanyId as string,
            this.getKeycloakPromise,
          );
        } catch (error) {
          console.error("Error fetching data for new company:", error);
        }
      }
    },
    async authenticated() {
      await this.setUploaderRightsForUser();
    },
  },
  components: {
    ClaimOwnershipPanel,
    CompanyInfoSheet,
    FrameworkSummaryPanel,
    TheContent,
    TheHeader,
    TheFooter,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      authenticated: inject<boolean>("authenticated"),
    };
  },
  created() {
    void this.setUploaderRightsForUser();
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
      isUserDataOwner: false,
      hasCompanyDataOwner: false,
      isUserAllowedToUpload: false,
      footerContent,
    };
  },
  mounted() {
    void this.getAggregatedFrameworkDataSummary();
    void this.updateHasCompanyDataOwner();
  },
  methods: {
    /**
     * updates the hasCompanyDataOwner in an async way
     */
    async updateHasCompanyDataOwner() {
      this.hasCompanyDataOwner = await hasCompanyAtLeastOneDataOwner(this.companyId, this.getKeycloakPromise);
    },
    /**
     * Retrieves the aggregated framework data summary
     */
    async getAggregatedFrameworkDataSummary(): Promise<void> {
      const companyDataControllerApi = new ApiClientProvider(this.getKeycloakPromise()).backendClients
        .companyDataController;
      this.aggregatedFrameworkDataSummary = (
        await companyDataControllerApi.getAggregatedFrameworkDataSummary(this.companyId)
      ).data as { [key in DataTypeEnum]: AggregatedFrameworkDataSummary } | undefined;
    },

    /**
     * Set if the user is allowed to upload data for the current company
     * @returns a promise that resolves to void, so the successful execution of the function can be awaited
     */
    async setUploaderRightsForUser() {
      if (this.authenticated) {
        await isUserDataOwnerForCompany(this.companyId, this.getKeycloakPromise)
          .then((result) => {
            this.isUserDataOwner = result;
            this.isUserAllowedToUpload = result;
          })
          .then(() => {
            if (!this.isUserAllowedToUpload) {
              return checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise).then((result) => {
                this.isUserAllowedToUpload = result;
              });
            }
          });
      }
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
