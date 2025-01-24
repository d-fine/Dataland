<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="paper-section flex">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="true" />
    <div class="card-wrapper">
      <div class="card-grid">
        <ClaimOwnershipPanel v-if="isClaimPanelVisible" :company-id="companyId" />

        <FrameworkSummaryPanel
          v-for="framework of FRAMEWORKS_WITH_VIEW_PAGE"
          :key="framework"
          :is-user-allowed-to-view="authenticated === true"
          :is-user-allowed-to-upload="isUserAllowedToUploadForFramework(framework)"
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
import { defineComponent, inject } from 'vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import { type AggregatedFrameworkDataSummary, type DataTypeEnum } from '@clients/backend';
import { ApiClientProvider } from '@/services/ApiClients';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import type Keycloak from 'keycloak-js';
import FrameworkSummaryPanel from '@/components/resources/companyCockpit/FrameworkSummaryPanel.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import { FRAMEWORKS_WITH_VIEW_PAGE, PRIVATE_FRAMEWORKS } from '@/utils/Constants';
import ClaimOwnershipPanel from '@/components/resources/companyCockpit/ClaimOwnershipPanel.vue';
import { checkIfUserHasRole, KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakUtils';
import { hasCompanyAtLeastOneCompanyOwner } from '@/utils/CompanyRolesUtils';
import { isCompanyIdValid } from '@/utils/ValidationUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';

export default defineComponent({
  name: 'CompanyCockpitPage',
  components: {
    ClaimOwnershipPanel,
    CompanyInfoSheet,
    FrameworkSummaryPanel,
    TheContent,
    TheHeader,
    TheFooter,
  },
  props: {
    companyId: {
      type: String,
      required: true,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      authenticated: inject<boolean>('authenticated'),
      companyRoleAssignments: inject<Array<CompanyRoleAssignment>>('companyRoleAssignments'),
      injectedUseMobileView: inject<boolean>('useMobileView'),
    };
  },
  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      aggregatedFrameworkDataSummary: undefined as
        | { [key in DataTypeEnum]: AggregatedFrameworkDataSummary }
        | undefined,
      FRAMEWORKS_WITH_VIEW_PAGE,
      isUserCompanyOwnerOrUploader: false,
      isUserKeycloakUploader: false,
      isAnyCompanyOwnerExisting: false,
      hasUserAnyRoleInCompany: false,
      footerContent,
    };
  },
  computed: {
    useMobileView() {
      return this.injectedUseMobileView;
    },
    isClaimPanelVisible() {
      return !this.isAnyCompanyOwnerExisting && isCompanyIdValid(this.companyId);
    },
  },
  watch: {
    async companyId(newCompanyId, oldCompanyId) {
      if (newCompanyId !== oldCompanyId) {
        try {
          await this.getAggregatedFrameworkDataSummary();
          await this.setUserRights();
        } catch (error) {
          console.error('Error fetching data for new company:', error);
        }
      }
    },
    async authenticated() {
      await this.setUserRights();
    },
  },

  created() {
    void this.setUserRights();
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

    /**
     * Checks if the user is allowed to upload datasets for the framework
     * @param framework to check for
     * @returns a boolean as result of this check
     */
    isUserAllowedToUploadForFramework(framework: DataTypeEnum): boolean {
      return this.isUserCompanyOwnerOrUploader || (this.isFrameworkPublic(framework) && this.isUserKeycloakUploader);
    },

    /**
     * Checks if the framework is a public framework
     * @param framework to check for
     * @returns a boolean as result of this check
     */
    isFrameworkPublic(framework: DataTypeEnum): boolean {
      return !PRIVATE_FRAMEWORKS.includes(framework);
    },

    /**
     * Set user access rights
     */
    async setUserRights() {
      this.isAnyCompanyOwnerExisting = await hasCompanyAtLeastOneCompanyOwner(this.companyId, this.getKeycloakPromise);
      const companyRoleAssignmentsOfUser = this.companyRoleAssignments;
      if (companyRoleAssignmentsOfUser) {
        const companyRolesForCompanyId = companyRoleAssignmentsOfUser
          .filter((it) => it.companyId === this.companyId)
          .map((it) => it.companyRole);
        this.hasUserAnyRoleInCompany = companyRolesForCompanyId.length > 0;
        this.isUserCompanyOwnerOrUploader =
          companyRolesForCompanyId.includes(CompanyRole.CompanyOwner) ||
          companyRolesForCompanyId.includes(CompanyRole.DataUploader);
      }
      this.isUserKeycloakUploader = await checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise);
    },
  },
});
</script>

<style lang="scss" scoped>
@use '@/assets/scss/newVariables' as *;

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
